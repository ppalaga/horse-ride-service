mvn = "mvn -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
pipeline {
    agent {
        label 'maven'
    }
    stages {
        stage('Build and test') {
            steps {
                sh "${mvn} clean install -DskipTests"
            }
        }
    }
}
