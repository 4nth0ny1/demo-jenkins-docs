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
        disableConcurrentBuilds()
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
                   String smokeName = "demo-smoke-test-${env.BUILD_TAG}".replaceAll('[^A-Za-z0-9_.-]', '-')

                   echo "Smoke container name: ${smokeName}"

                   // Cleanup from any previous attempt
                   sh "docker rm -f ${smokeName} || true"

                   echo "Running container for smoke test (no --rm so we can read logs if it crashes)..."
                   sh "docker run -d --name ${smokeName} ${env.APP_NAME}:${env.BUILD_NUMBER}"

                   echo "Give the app a moment to start..."
                   sh "sleep 3"

                   // If the container already died, fail immediately with logs
                   sh """
                       RUNNING=\$(docker inspect -f '{{.State.Running}}' ${smokeName} 2>/dev/null || echo 'false')
                       if [ "\$RUNNING" != "true" ]; then
                           echo "Smoke container is NOT running (it likely crashed)."
                           echo "Container state:"
                           docker inspect ${smokeName} || true
                           echo "Container logs:"
                           docker logs ${smokeName} || true
                           exit 1
                       fi
                   """

                   echo "Checking /api/products endpoint with retries..."
                   sh """
                       ATTEMPTS=0
                       MAX_ATTEMPTS=10
                       STATUS=000

                       while [ \$ATTEMPTS -lt \$MAX_ATTEMPTS ]; do
                           # If container disappears, stop retrying and dump logs
                           EXISTS=\$(docker ps -q -f name=^/${smokeName}\$ || true)
                           if [ -z "\$EXISTS" ]; then
                               echo "Smoke container is no longer running."
                               echo "Container logs:"
                               docker logs ${smokeName} || true
                               exit 1
                           fi

                           STATUS=\$(docker run --rm --network container:${smokeName} curlimages/curl:latest \\
                               -s -o /dev/null -w '%{http_code}' ${env.SMOKE_URL} || true)

                           echo "HTTP status: \$STATUS"

                           if [ "\$STATUS" = "200" ]; then
                               echo "Smoke test passed"
                               exit 0
                           fi

                           ATTEMPTS=\$((ATTEMPTS+1))
                           sleep 3
                       done

                       echo "Smoke test FAILED after \$MAX_ATTEMPTS attempts. Last status: \$STATUS"
                       echo "Container logs:"
                       docker logs ${smokeName} || true
                       exit 1
                   """
               }
           }
           post {
               always {
                   script {
                       String smokeName = "demo-smoke-test-${env.BUILD_TAG}".replaceAll('[^A-Za-z0-9_.-]', '-')
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
