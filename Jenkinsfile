mvn = "mvn -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
pgDb = "ride"
pgUser = "pgUser1"
pgPassword = "pgPassword123"
cicdProject = "horse-ride-cicd"
stageProject = "horse-ride-stage"
productionProject = "horse-ride-prod"
pipeline {
    agent {
        label 'maven'
    }
    stages {
        stage('Build and test') {
            steps {
                script {
                    /* Let's use the OpenShift client delivered by OpenShift Jenkins Pipeline (DSL) Plugin
                     * see https://github.com/openshift/jenkins-client-plugin
                     * and https://jenkins-horse-ride-cicd.$(minishift ip).nip.io/pipeline-syntax/globals */
                    openshift.withCluster() {
                        openshift.withProject(cicdProject) {
                            deployPostgresIfNeeded(openshift)
                            scale('horse-ride-service-postgresql', 1, true)
                        }
                    }

                    /* Maven build with unit tests */
                    sh (
                        "RIDE_DB_HOST=horse-ride-service-postgresql" +
                        " RIDE_DB_PORT=5432" +
                        " RIDE_DB_NAME=${pgDb}" +
                        " RIDE_DB_USERNAME=${pgUser}" +
                        " RIDE_DB_PASSWORD=${pgPassword}" +
                        " RIDE_DB_NAME=${pgDb}" +
                        " RIDE_DB_DLL_AUTO=update" +
                        " ${mvn} clean install"
                    )
                }
            }
        }
        stage('Build and test image') {
            steps {
                script {

                    /* fabric8-maven-plugin builds the image and pushes it to the cluster's internal Docker registry */
                    sh ("${mvn} fabric8:build -Popenshift")

                    openshift.withCluster() {
                        openshift.withProject(cicdProject) {

                            /* Get the sha256 hash of the image build by fabric8:build to be able to check that we are
                             * promoting the right image through the environments */
                            def isTag = openshift.selector('istag/horse-ride-service:test').object();
                            imageId = isTag.image.metadata.name
                            println "Built image "+ imageId

                            def dc = openshift.selector('dc/horse-ride-service')
                            if (!dc.exists()) {
                                /* The deploymentconfig does not exist yet - the pipeline is probably run for the first time */
                                openshift.newApp(
                                    "--name=horse-ride-service",
                                    "-e", "RIDE_DB_HOST=horse-ride-service-postgresql",
                                    "-e", "RIDE_DB_PORT=5432",
                                    "-e", "RIDE_DB_NAME=${pgDb}",
                                    "-e", "RIDE_DB_USERNAME=${pgUser}",
                                    "-e", "RIDE_DB_PASSWORD=${pgPassword}",
                                    "-e", "RIDE_DB_DLL_AUTO=update",
                                    "-i", "${cicdProject}/horse-ride-service:test"
                                )
                                /* Use Spring Boot Actuator's /health endpoint as a readiness and liveness probe */
                                openshift.set('probe', 'dc/horse-ride-service', '--readiness',
                                    '--get-url=http://:8080/health', '--initial-delay-seconds=5', '--period-seconds=2')
                                openshift.set('probe', 'dc/horse-ride-service', '--liveness',
                                    '--get-url=http://:8080/health', '--initial-delay-seconds=10', '--period-seconds=2')
                            } else {
                                /* nothing to do here: the deploymentconfig exist
                                 * and a new deployment was already triggered by push to docker registry
                                 * by ${mvn} fabric8:build above */
                                scale('horse-ride-service', 1, false)
                            }

                            /* The deployment triggered by new-app or push to image stream needs some time to finish
                               oc rollout status --watch makes this script wait till the deployment is ready */
                            dc.rollout().status('--watch')

                            /* Run the integration tests against the running container */
                            sh ("HORSE_RIDE_SERVICE_HOST=horse-ride-service" +
                                " HORSE_RIDE_SERVICE_PORT=8080" +
                                " ${mvn} failsafe:integration-test failsafe:verify -Popenshift")

                            /* Idle these to free some system resources */
                            scale('horse-ride-service', 0, false)
                            scale('horse-ride-service-postgresql', 0, false)
                        }
                    }
                }
            }
        }
        stage('Stage') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject(stageProject) {
                            deployPostgresIfNeeded(openshift)
                            scale('horse-ride-service-postgresql', 1, true)

                            /* Promote the image from test to stage by re-tagging it.
                             * This will trigger a rollout of the new image if the DeploymentConfig
                             * based on that tag exists already */
                            openshift.tag("${cicdProject}/horse-ride-service:test", "${stageProject}/horse-ride-service:stage")

                            def dc = openshift.selector('dc/horse-ride-service')
                            if (!dc.exists()) {
                                /* The deploymentconfig does not exist yet - the pipeline is probably run for the first time */
                                def serviceApp = openshift.newApp(
                                    "--name=horse-ride-service",
                                    "-e", "RIDE_DB_HOST=horse-ride-service-postgresql",
                                    "-e", "RIDE_DB_PORT=5432",
                                    "-e", "RIDE_DB_NAME=${pgDb}",
                                    "-e", "RIDE_DB_USERNAME=${pgUser}",
                                    "-e", "RIDE_DB_PASSWORD=${pgPassword}",
                                    "-e", "RIDE_DB_DLL_AUTO=update",
                                    "-i", "${stageProject}/horse-ride-service:stage"
                                )
                                /* Use Spring Boot Actuator's /health endpoint as a readiness and liveness probe */
                                openshift.set('probe', 'dc/horse-ride-service', '--readiness',
                                    '--get-url=http://:8080/health', '--initial-delay-seconds=5', '--period-seconds=2')
                                openshift.set('probe', 'dc/horse-ride-service', '--liveness',
                                    '--get-url=http://:8080/health', '--initial-delay-seconds=10', '--period-seconds=2')

                                def serviceRoute = openshift.selector('route/horse-ride-service');
                                if (!serviceRoute.exists()) {
                                    serviceApp.narrow('svc').expose()
                                } else {
                                    def routeModel = serviceRoute.object()
                                    routeModel.spec.to.name = "horse-ride-service"
                                    openshift.apply(routeModel)
                                }
                            } else {
                                /* nothing to do here: the deploymentconfig exists
                                 * and a new deployment was already triggered by tagging the image above */
                            }
                            scale('horse-ride-service', 1, false)

                            /* The deployment triggered by new-app or push to image stream needs some time to finish
                             * oc rollout status --watch makes this script wait till the deployment is ready */
                            dc.rollout().status('--watch')

                            def serviceUrl = 'http://' + openshift.selector('route/horse-ride-service').object().spec.host

                            /* Ask for manual approval */
                            input 'Promote the staged image ' + serviceUrl + ' to Production?'

                            /* Idle these to free some system resources */
                            scale('horse-ride-service', 0, false)
                            scale('horse-ride-service-postgresql', 0, false)
                        }
                    }
                }
            }
        }
        stage('Production') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject(productionProject) {
                            deployPostgresIfNeeded(openshift)

                            def newColor = 'blue'
                            def oldColor = 'green'
                            def route = openshift.selector('route/horse-ride-service')
                            if (route.exists()) {
                                def activeService = route.object().spec.to.name
                                if (activeService.endsWith('blue')) {
                                    newColor = 'green'
                                    oldColor = 'blue'
                                }
                            }
                            echo "The new color is ${newColor}"

                            openshift.tag("${stageProject}/horse-ride-service:stage", "${productionProject}/horse-ride-service-${newColor}:production")

                            def dc = openshift.selector("dc/horse-ride-service-${newColor}")
                            if (!dc.exists()) {
                                /* The deploymentconfig does not exist yet - the pipeline is probably run for the first time */
                                def serviceApp = openshift.newApp(
                                    "--name=horse-ride-service-${newColor}",
                                    "-e", "RIDE_DB_HOST=horse-ride-service-postgresql",
                                    "-e", "RIDE_DB_PORT=5432",
                                    "-e", "RIDE_DB_NAME=${pgDb}",
                                    "-e", "RIDE_DB_USERNAME=${pgUser}",
                                    "-e", "RIDE_DB_PASSWORD=${pgPassword}",
                                    "-e", "RIDE_DB_DLL_AUTO=update",
                                    "-i", "${productionProject}/horse-ride-service-${newColor}:production"
                                )

                            } else {
                                /* nothing to do here: the deploymentconfig exists
                                 * and a new deployment was already triggered by tagging the image above */
                            }

                            /* Use Spring Boot Actuator's /health endpoint as a readiness and liveness probe */
                            openshift.set('probe', "dc/horse-ride-service-${newColor}", '--readiness',
                                '--get-url=http://:8080/api/unhealth', '--initial-delay-seconds=5', '--period-seconds=2')
                            openshift.set('probe', "dc/horse-ride-service-${newColor}", '--liveness',
                                '--get-url=http://:8080/api/unhealth', '--initial-delay-seconds=15', '--period-seconds=2')

                            scale("horse-ride-service-${newColor}", 1, false)

                            /* The deployment triggered by new-app or push to image stream needs some time to finish
                             * oc rollout status --watch makes this script wait till the deployment is ready */
                            dc.rollout().status('--watch')

                            def newColorRoute = openshift.selector("route/horse-ride-service-${newColor}")
                            if (!newColorRoute.exists()) {
                                openshift.selector("svc/horse-ride-service-${newColor}").expose()
                            }
                            def newColorHealthUrl = "http://"+ newColorRoute.object().spec.host +"/api/unhealth"

                            /* Take the old color-less resources down if they still exist to save some system resources */
                            deleteExisting("svc/horse-ride-service", "dc/horse-ride-service", "is/horse-ride-service")

                            if (!route.exists()) {
                                /* No route yet, probably the first execution of the pipeline */
                                openshift.selector("svc/horse-ride-service-${newColor}").expose("--name=horse-ride-service")
                            } else {

                                /* Send 50% of the traffic to the new version of the service */
                                echo "Splitting trafic old 50 : new 50"
                                splitTraffic(openshift, oldColor, 50, newColor, 50)
                                def log = [1,1,1]
                                for (int i = 0; i < 10; i++) {
                                    sleep 2
                                    def healthStatus = sh (
                                        script: "curl --connect-timeout 1 -o /dev/null -X GET -sL -w '%{http_code}' -H 'Content-Type: application/json' ${newColorHealthUrl}",
                                        returnStdout: true
                                    )
                                    echo "The health endpoint returned "+ healthStatus
                                    log[i % 3] = (healthStatus == "200" ? 1 : 0)
                                    if (log.sum() == 0) {
                                        /* rollback because of 3 consecutive failures */
                                        splitTraffic(openshift, oldColor, 100, newColor, 0)
			                            def serviceUrl = 'http://' + openshift.selector('route/horse-ride-service').object().spec.host
                                        echo "Canary died :( reverting the route to send all traffic to original image: ${serviceUrl}"
                                        error("Canary died")
                                    }
                                }
                            }
                            /* Canary survived for ~20 seconds - let's consider it stable */
                            splitTraffic(openshift, newColor, 100, oldColor, 0)

                            /* Idle the old version to save some system resources */
                            scale("horse-ride-service-${oldColor}", 0, false)

                            def serviceUrl = 'http://' + openshift.selector('route/horse-ride-service').object().spec.host
                            echo 'Canary survived and now serves 100% of prod traffic: ' + serviceUrl
                        }
                    }
                }
            }
        }
    }
}

def deployPostgresIfNeeded(openshift) {
    def dbDc = openshift.selector("dc/horse-ride-service-postgresql");
    if (!dbDc.exists()) {
        def dbApp = openshift.newApp(
            "--name=horse-ride-service-postgresql",
            "-e", "POSTGRESQL_USER=${pgUser}",
            "-e", "POSTGRESQL_PASSWORD=${pgPassword}",
            "-e", "POSTGRESQL_DATABASE=${pgDb}",
            "centos/postgresql-95-centos7"
        )
        /* The deployment triggered by new-app needs some time to finish
           oc rollout status --watch makes this script wait till the deployment is ready */
        dbApp.narrow('dc').rollout().status('--watch')
    }
}

def scale(String dcName, int replicas, boolean watch) {
    def dc = openshift.selector("dc/${dcName}")
    if (dc.exists() && dc.object().spec.replicas != replicas) {
        dc.scale("--replicas=${replicas}")
        if (watch) {
            dc.rollout().status('--watch')
        }
    }
}

def splitTraffic(openshift, primaryColor, primaryWeight, altColor, altWeight) {
    def routeModel = openshift.selector('route/horse-ride-service').object()
    def primaryService = routeModel.spec.to
    primaryService.name = "horse-ride-service-${primaryColor}"
    primaryService.weight = primaryWeight
    routeModel.spec.alternateBackends = [
        [
            kind: "Service",
            name: "horse-ride-service-${altColor}",
            weight: altWeight
        ]
    ]
    openshift.apply(routeModel)
}

@NonCPS
def deleteExisting(String... kindNames) {
    for (String kindName : kindNames) {
        def sel = openshift.selector(kindName)
        if (sel.exists()) {
            sel.delete()
        }
    }
    return
}
