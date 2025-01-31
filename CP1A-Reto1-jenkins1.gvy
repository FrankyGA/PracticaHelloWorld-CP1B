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
                dir('C:\\ProgramData\\Jenkins\\.jenkins\\workspace\\Practica 1\\Jenkins 1') {
                    git branch: 'master', url: 'https://github.com/FrankyGA/PracticaHelloWorld.git'
                    echo 'Repositorio clonado en la ruta especificada.'
                }
            }
        }

        stage('Verificar Código Descargado') {
            steps {
                echo 'Verificando archivos descargados...'
                bat 'dir "C:\\ProgramData\\Jenkins\\.jenkins\\workspace\\Practica 1\\Jenkins 1"'
            }
        }

        stage('Verificar Espacio de Trabajo') {
            steps {
                echo "El espacio de trabajo es: ${env.WORKSPACE}"
            }
        }

        stage('Build') {
            steps {
                echo 'Ejecutando etapa de Build...'
                // Build vacio
            }
        }
    }
}