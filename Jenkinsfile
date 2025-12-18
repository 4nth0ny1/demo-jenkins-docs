pipeline {
    agent {
        docker {
            image 'maven:3.9.11-eclipse-temurin-21-alpine'
            args '''
                -v $HOME/.m2:/root/.m2
                -v /var/run/docker.sock:/var/run/docker.sock
            '''
        }
    }

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        APP_NAME = 'demo-app'

        // CI tests run in Jenkins workspace (NOT inside your runtime Docker image),
        // so keep SQLite in target/ where we always have write access.
        CI_DB_URL = 'jdbc:sqlite:target/ci-demo.db'

        // Runtime container DB path (exists + writable due to Dockerfile)
        CONTAINER_DB_URL = 'jdbc:sqlite:/app/data/demo.db'

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
                sh """
                    mkdir -p target
                    mvn -B -ntp test -Dspring.datasource.url='${CI_DB_URL}'
                """
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
                    String smokeName = "demo-smoke-${env.BUILD_TAG}".replaceAll('[^A-Za-z0-9_.-]', '-')
                    echo "Smoke container name: ${smokeName}"

                    sh "docker rm -f ${smokeName} || true"

                    echo "Starting app container..."
                    sh """
                        docker run -d --name ${smokeName} \
                          -e SPRING_DATASOURCE_URL='${CONTAINER_DB_URL}' \
                          ${APP_NAME}:${BUILD_NUMBER}
                    """

                    echo "Wait briefly for startup..."
                    sh "sleep 3"

                    // If it died immediately, show logs and fail now.
                    sh """
                        RUNNING=\$(docker inspect -f '{{.State.Running}}' ${smokeName} 2>/dev/null || echo 'false')
                        if [ "\$RUNNING" != "true" ]; then
                            echo "Container crashed during startup."
                            docker inspect ${smokeName} || true
                            docker logs ${smokeName} || true
                            exit 1
                        fi
                    """

                    echo "Hitting ${SMOKE_URL} with retries..."
                    sh """
                        ATTEMPTS=0
                        MAX_ATTEMPTS=10
                        STATUS=000

                        while [ \$ATTEMPTS -lt \$MAX_ATTEMPTS ]; do
                            STATUS=\$(docker run --rm --network container:${smokeName} curlimages/curl:latest \\
                                -s -o /dev/null -w '%{http_code}' ${SMOKE_URL} || true)

                            echo "HTTP status: \$STATUS"

                            if [ "\$STATUS" = "200" ]; then
                                echo "Smoke test passed"
                                exit 0
                            fi

                            ATTEMPTS=\$((ATTEMPTS+1))
                            sleep 3
                        done

                        echo "Smoke test FAILED after \$MAX_ATTEMPTS attempts."
                        docker logs ${smokeName} || true
                        exit 1
                    """
                }
            }
            post {
                always {
                    script {
                        String smokeName = "demo-smoke-${env.BUILD_TAG}".replaceAll('[^A-Za-z0-9_.-]', '-')
                        sh "docker rm -f ${smokeName} || true"
                    }
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
