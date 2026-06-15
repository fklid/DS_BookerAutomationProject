pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Make gradlew executable') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean build -x test'
            }
        }

        stage('Run tests') {
            steps {
                sh './gradlew test'
            }
            post {
                always {
                    // Публикация JUnit отчётов
                    junit '**/build/test-results/test/*.xml'

                    // Публикация Allure отчётов (если плагин установлен)
                    allure includeProperties: false,
                           jdk: '',
                           results: [[path: 'build/allure-results']]
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}