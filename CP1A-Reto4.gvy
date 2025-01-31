pipeline {
    agent any
    stages {
        stage('Preparación') {
            steps {
							echo 'Pipeline iniciado. Preparando entorno.'
            }
        }
        stage('Clonar Repositorio') {
            steps {
									echo 'Clonando el repositorio...'
									dir('C:\\ProgramData\\Jenkins\\.jenkins\\workspace\\PruebaJenkins2') {
									git branch: 'master', url: 'https://github.com/FrankyGA/PracticaHelloWorld.git'
                  echo 'Repositorio clonado en la ruta especificada.'
                }
            }
        }
        stage('Verificar Código Descargado') {
            steps {
                echo 'Verificando archivos descargados...'
								bat 'dir "C:\\ProgramData\\Jenkins\\.jenkins\\workspace\\PruebaJenkins2"'
            }
        }
        stage('Verificar espacio de trabajo') {
            steps {
                script {
                  echo "El espacio de trabajo es: ${WORKSPACE}"
                }
            }
        }
        stage('Build') {
            steps {
              echo 'Ejecutando etapa de Build...'
            }
        }
        stage('Pruebas en paralelo') {
            parallel {
                stage('Unit') {
                    steps {
                        lock(resource: 'rest-tests') {
                            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                                bat '''
                                set PYTHONPATH=%WORKSPACE%
                                python -m pytest --junitxml=result-unit.xml test\\unit
                                '''
                            }
                        }
                    }
                }
                stage('Service') {
                    steps {
                        lock(resource: 'rest-tests') {
                            script {
                                bat '''
                                docker rm -f wiremock || true docker run -d --name wiremock -p 9090:8080 -v C:/ProgramData/Jenkins/.jenkins/workspace/PruebaJenkins2/test/wiremock/mappings:/home/wiremock/mappings wiremock/wiremock:latest
                                set FLASK_APP=app\\api.py
                                start /B python -m flask run --host=0.0.0.0 --port=5000
                                set PYTHONPATH=%WORKSPACE%
                                python -m pytest --junitxml=result-rest.xml test//rest
                                '''
                            }
                        }
                    }
                }
            }
        }
        stage('Results') {
            steps {
                // Publicar los resultados.
                junit 'result-unit.xml'
                junit 'result-rest.xml'
            }
        }
    }
}