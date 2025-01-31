pipeline {
    agent none

    stages {
        stage('Preparación') {
            agent { label 'principal' }
            steps {
                echo 'Pipeline iniciado. Preparando entorno.'
                bat 'whoami'
                bat 'hostname'
                echo "Workspace: ${env.WORKSPACE}"
            }
        }

        stage('GetCode') {
            agent { label 'agent1' }
            steps {
                git 'https://github.com/FrankyGA/PracticaHelloWorld.git'
                stash includes: '**', name: 'source'
                bat 'whoami'
                bat 'hostname'
                echo "Workspace: ${env.WORKSPACE}"
            }
        }

        stage('Unit Tests') {
            agent { label 'agent1' }
            steps {
                unstash 'source'
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    bat '''
                        set PYTHONPATH=%WORKSPACE%
                        pytest --junitxml=result-unit.xml test\\unit
                    '''
                }
                stash includes: 'result-unit.xml', name: 'unit-test-results'
                bat 'whoami'
                bat 'hostname'
                echo "Workspace: ${env.WORKSPACE}"
            }
        }

        stage('Results') {
            agent { label 'principal' }
            steps {
                unstash 'unit-test-results'
                junit 'result-unit.xml'
                bat 'whoami'
                bat 'hostname'
                echo "Workspace: ${env.WORKSPACE}"
            }
        }
    }

    post {
        always {
            node('principal') {
                cleanWs()
            }
        }
    }
}