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
