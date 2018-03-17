#!/bin/bash

set -e
set -x

ARG_PROJECT_SUFFIX=
ARG_OC_OPS=
PRJ_CI="horse-ride-cicd${ARG_PROJECT_SUFFIX}"
PRJ_STAGE="horse-ride-stage${ARG_PROJECT_SUFFIX}"
PRJ_PROD="horse-ride-prod${ARG_PROJECT_SUFFIX}"

GOGS_ADMIN_USER=team
GOGS_ADMIN_PASSWORD=team

function create_projects() {
  _PROJECTS=$@
  for project in $_PROJECTS
  do
    # We need to retry the new-project command because in case the project was recently deleted, the cluster needs some
    # time to clean all rests of the project
    for i in {1..40}
    do
      if oc $ARG_OC_OP new-project ${project} > /dev/null
      then
        break
      fi
      sleep 2
    done
  done
}

function configure_project_permissions() {
  _PROJECTS=$@
  for project in $_PROJECTS
  do
    oc $ARG_OC_OP adm policy add-role-to-group admin system:serviceaccounts:${PRJ_CI} -n $project >/dev/null 2>&1
    oc $ARG_OC_OP adm policy add-role-to-group admin system:serviceaccounts:$project -n $project >/dev/null 2>&1
  done

  if [ $LOGGEDIN_USER == 'system:admin' ] ; then
    for project in $_PROJECTS
    do
      oc $ARG_OC_OP adm policy add-role-to-user admin $ARG_USERNAME -n $project >/dev/null 2>&1
      oc $ARG_OC_OP annotate --overwrite namespace $project demo=demo1-$PRJ_SUFFIX demo=demo-modern-arch-$PRJ_SUFFIX >/dev/null 2>&1
    done
    oc $ARG_OC_OP adm pod-network join-projects --to=${PRJ_CI} $_PROJECTS >/dev/null 2>&1
  fi

  # Hack to extract domain name when it's not determine in
  # advanced e.g. <user>-<project>.4s23.cluster
  oc $ARG_OC_OP create route edge testroute --service=testsvc --port=80 -n ${PRJ_PROD} >/dev/null
  DOMAIN=$(oc $ARG_OC_OP get route testroute -o template --template='{{.spec.host}}' -n ${PRJ_PROD} | sed "s/testroute-${PRJ_PROD}.//g")
  GOGS_ROUTE="gogs-${PRJ_CI}.$DOMAIN"
  oc $ARG_OC_OP delete route testroute -n ${PRJ_PROD} >/dev/null
}


function deploy_gogs() {

  local _TEMPLATE="gogs-persistent-template.yaml"

  local _DB_USER=gogs
  local _DB_PASSWORD=gogs
  local _DB_NAME=gogs

  echo "Using template $_TEMPLATE"
  oc $ARG_OC_OP process -f $_TEMPLATE --param=HOSTNAME=$GOGS_ROUTE --param=GOGS_VERSION=0.11.29 --param=DATABASE_USER=$_DB_USER --param=DATABASE_PASSWORD=$_DB_PASSWORD --param=DATABASE_NAME=$_DB_NAME --param=SKIP_TLS_VERIFY=true -n ${PRJ_CI} | oc $ARG_OC_OP create -f - -n ${PRJ_CI}

  sleep 5

  # wait for Gogs to be ready
  oc $ARG_OC_OP rollout status dc gogs-postgresql -w -n ${PRJ_CI}
  oc $ARG_OC_OP rollout status dc gogs -w -n ${PRJ_CI}

  # add admin user
  for i in {1..10}
  do
    sleep 5
    _RETURN=$(curl -o /dev/null -sL --post302 -w "%{http_code}" http://$GOGS_ROUTE/user/sign_up \
      --form user_name=$GOGS_ADMIN_USER \
      --form password=$GOGS_ADMIN_PASSWORD \
      --form retype=$GOGS_ADMIN_PASSWORD \
      --form email=$GOGS_ADMIN_USER@gogs.com)
    if [ "200" == "$_RETURN" ]
    then
      break
    fi
  done


  # import GitHub repo
  _DATA_JSON='{ "name": "horse-ride-service", "private": false }'

  _RETURN=$(curl -o /dev/null -sL -w "%{http_code}" -H "Content-Type: application/json" -d "$_DATA_JSON" -u $GOGS_ADMIN_USER:$GOGS_ADMIN_PASSWORD -X POST http://$GOGS_ROUTE/api/v1/user/repos)
  if [ $_RETURN != "201" ] && [ $_RETURN != "200" ] ; then
    echo "WARNING: Failed (http code $_RETURN) to create repository"
  else
    echo "horse-ride-service repo created"
  fi

  sleep 2
  git push -f http://$GOGS_ADMIN_USER:$GOGS_ADMIN_PASSWORD@$GOGS_ROUTE/$GOGS_ADMIN_USER/horse-ride-service.git master

}


set +e
oc $ARG_OC_OP delete project ${PRJ_CI} --now=true > /dev/null
oc $ARG_OC_OP delete project ${PRJ_STAGE} --now=true > /dev/null
oc $ARG_OC_OP delete project ${PRJ_PROD} --now=true > /dev/null
set -e

create_projects ${PRJ_CI} ${PRJ_STAGE} ${PRJ_PROD}

configure_project_permissions ${PRJ_CI} ${PRJ_STAGE} ${PRJ_PROD}

deploy_gogs

oc new-app http://$GOGS_ROUTE/$GOGS_ADMIN_USER/horse-ride-service.git -n ${PRJ_CI}

BC_SECRET=$(oc get bc horse-ride-service -n ${PRJ_CI} -o 'jsonpath={.spec.triggers[?(@.generic)].generic.secret}')
MINISHIFT_IP=$(minishift ip)
PAYLOAD_URL="https://${MINISHIFT_IP}:8443/oapi/v1/namespaces/${PRJ_CI}/buildconfigs/horse-ride-service/webhooks/${BC_SECRET}/generic"

_DATA_JSON=$(cat <<EOF
{
    "type": "gogs",
    "config": {
        "content_type": "json",
        "url": "${PAYLOAD_URL}"
    },
    "events": [ "push" ],
    "active": true
}
EOF
)

_RETURN=$(curl -o /dev/null -sL -w "%{http_code}" -H "Content-Type: application/json" -d "$_DATA_JSON" -u $GOGS_ADMIN_USER:$GOGS_ADMIN_PASSWORD -X POST http://$GOGS_ROUTE/api/v1/repos/team/horse-ride-service/hooks)
if [ $_RETURN != "201" ] && [ $_RETURN != "200" ] ; then
  echo "Failed (http code $_RETURN) to create a gogs repo hook"
  exit 1
else
  echo "Gogs git repo hook created"
fi
