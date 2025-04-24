//Jenkinsfile (Example)

@Library('f3lib') _

pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo "Building..."
            }
        }
    }

    post {
        always {
            commonCleanup()
        }
        success {
            commonNotify(true)
        }
        failure {
            commonNotify(false)
        }
    }
}