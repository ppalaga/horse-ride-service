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
                            }

                            /* The deployment triggered by new-app or push to image stream needs some time to finish
                               oc rollout status --watch makes this script wait till the deployment is ready */
                            dc.rollout().status('--watch')

                            /* Run the integration tests against the running container */
                            sh ("HORSE_RIDE_SERVICE_HOST=horse-ride-service" +
                                " HORSE_RIDE_SERVICE_PORT=8080" +
                                " ${mvn} failsafe:integration-test failsafe:verify -Popenshift")
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
