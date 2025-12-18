pipeline {
    agent {
        docker {
            image 'maven:3.9.11-eclipse-temurin-21-alpine'

            // Maven cache + Docker daemon access (needed for docker build/run)
            args '''
                -v $HOME/.m2:/root/.m2
                -v /var/run/docker.sock:/var/run/docker.sock
            '''
        }
    }

    options {
        timestamps()
    }

    environment {
        APP_NAME = 'demo-app'
        CONTAINER_NAME = 'demo-smoke-test'
        SMOKE_URL = 'http://localhost:8080/api/products'
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn -B -ntp clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B -ntp test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Quality') {
            steps {
                sh 'mvn -B -ntp jacoco:report jacoco:check spotbugs:check'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/site/**', fingerprint: false
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B -ntp -DskipTests package'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            steps {
                sh 'apk add --no-cache docker-cli'

                sh 'docker version'

                sh """
                    docker build \
                      -t ${APP_NAME}:${BUILD_NUMBER} \
                      -t ${APP_NAME}:${GIT_COMMIT} \
                      .
                """
            }
        }

        stage('Smoke Test') {
            steps {
                script {
                    sh "docker rm -f ${CONTAINER_NAME} || true"

                    sh "docker run -d --rm --name ${CONTAINER_NAME} ${APP_NAME}:${BUILD_NUMBER}"

                    sh """
                        ATTEMPTS=0
                        MAX_ATTEMPTS=10
                        STATUS=000

                        while [ \$ATTEMPTS -lt \$MAX_ATTEMPTS ]; do
                            STATUS=\$(docker run --rm --network container:${CONTAINER_NAME} curlimages/curl:latest \\
                                -s -o /dev/null -w '%{http_code}' ${SMOKE_URL} || true)

                            echo "HTTP status: \$STATUS"

                            if [ "\$STATUS" = "200" ]; then
                                echo "Smoke test passed"
                                exit 0
                            fi

                            ATTEMPTS=\$((ATTEMPTS+1))
                            sleep 3
                        done

                        echo "Smoke test FAILED after \$MAX_ATTEMPTS attempts. Last status: \$STATUS"
                        docker logs ${CONTAINER_NAME} || true
                        exit 1
                    """
                }
            }
            post {
                always {
                    sh "docker stop ${CONTAINER_NAME} || true"
                }
            }
        }

        stage('Info') {
            steps {
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Commit: ${env.GIT_COMMIT}"
                echo "Image tags:"
                echo "  ${APP_NAME}:${env.BUILD_NUMBER}"
                echo "  ${APP_NAME}:${env.GIT_COMMIT}"
            }
        }
    }
}
