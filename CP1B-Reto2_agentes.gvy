pipeline {
    agent none

    stages {
        stage('Get Code') {
            agent { label 'principal' }
            steps {
                git 'https://github.com/FrankyGA/PracticaHelloWorld-CP1B.git'
                bat 'dir'
                bat 'whoami'
                bat 'hostname'
                echo "WORKSPACE: ${env.WORKSPACE}"
            }
        }
        
        stage('Parallel Tests') {
            parallel {
                stage('JUnit Tests') {
                    agent { label 'agent1' }
                    steps {
                        node('agent1') {
                            ws('C:\\ProgramData\\Jenkins\\.jenkins\\workspace\\CP1B-Reto2') {
                                script {
                                    bat '''
                                        whoami
                                        hostname
                                        echo WORKSPACE: ${WORKSPACE}
                                        set PYTHONPATH=%WORKSPACE%
                                        C:\\Users\\kingl\\AppData\\Local\\Programs\\Python\\Python312\\Scripts\\pytest --junitxml=result-junit.xml test\\unit
                                    '''
                                    junit 'result-junit.xml'
                                }
                            }
                        }
                    }
                }
                stage('Static Analysis') {
                    agent { label 'agent1' }
                    steps {
                        node('agent1') {
                            ws('C:\\ProgramData\\Jenkins\\.jenkins\\workspace\\CP1B-Reto2') {
                                script {
                                    bat '''
                                        whoami
                                        hostname
                                        echo WORKSPACE: ${WORKSPACE}
                                        C:\\Users\\kingl\\AppData\\Local\\Programs\\Python\\Python312\\Scripts\\flake8 --exit-zero --format=pylint app >flake8.out
                                    '''
                                    recordIssues tools: [flake8(name: 'Flake8', pattern: 'flake8.out')], 
                                                 qualityGates: [
                                                     [threshold: 8, type: 'TOTAL', unstable: true], 
                                                     [threshold: 10, type: 'TOTAL', unhealthy: true]
                                                 ]
                                }
                            }
                        }
                    }
                }
                stage('Security') {
                    agent { label 'agent1' }
                    steps {
                        node('agent1') {
                            ws('C:\\ProgramData\\Jenkins\\.jenkins\\workspace\\CP1B-Reto2') {
                                script {
                                    bat '''
                                        whoami
                                        hostname
                                        echo WORKSPACE: ${WORKSPACE}
                                        C:\\Users\\kingl\\AppData\\Local\\Programs\\Python\\Python312\\Scripts\\bandit --exit-zero -r . -f custom -o bandit.out --msg-template "{abspath}:{line}: [{test_id}]: {msg}"
                                    '''
                                }
                                recordIssues tools: [pyLint(name: 'Bandit', pattern: 'bandit.out')], 
                                             qualityGates: [
                                                 [threshold: 2, type: 'TOTAL', unstable: true], 
                                                 [threshold: 4, type: 'TOTAL', unhealthy: true]
                                             ]
                            }
                        }
                    }
                }
            }
        }

        stage('Performance') {
            agent { label 'principal' }
            steps {
                script {
                    bat '''
                        whoami
                        hostname
                        echo WORKSPACE: ${WORKSPACE}
                        set FLASK_APP=app\\api.py
                        start /b C:\\Users\\kingl\\AppData\\Local\\Programs\\Python\\Python312\\Scripts\\flask.exe run
                        timeout /t 5
                        C:\\Users\\kingl\\apache-jmeter-5.6.3\\bin\\jmeter -n -t test\\jmeter\\suma-resta.jmx -f -l suma-resta.jtl
                        taskkill /F /IM flask.exe
                    '''
                }
                perfReport sourceDataFiles: 'suma-resta.jtl'
            }
        }

        stage('Coverage') {
            agent { label 'principal' }
            steps {
                script {
                    bat '''
                        whoami
                        hostname
                        echo WORKSPACE: ${WORKSPACE}
                        C:\\Users\\kingl\\AppData\\Local\\Programs\\Python\\Python312\\Scripts\\coverage run --branch --source=app --omit=app\\_init.py,app\\api.py -m pytest test\\unit
                        C:\\Users\\kingl\\AppData\\Local\\Programs\\Python\\Python312\\Scripts\\coverage xml
                    '''
                    catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                        cobertura coberturaReportFile: 'coverage.xml', 
                                  conditionalCoverageTargets: '80,0,90', 
                                  lineCoverageTargets: '85,0,95'
                    }
                }
            }
        }
    }
}