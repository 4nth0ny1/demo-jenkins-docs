pipeline {
    agent {
        docker {
            image 'maven:3.9.11-eclipse-temurin-21-alpine'
            // optional: cache Maven repo between builds
            args '-v $HOME/.m2:/root/.m2'
        }
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn -B -ntp clean compile'
            }
        }

        stage('Test') {
            steps {
                // compile the project (no tests here)
                sh 'mvn -B -ntp test'
            }
            post {
                always {
                    // so Jenkins still shows results even if tests fail.
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Quality') {
            steps {
                // Run coverage + Spotbugs
                sh 'mvn -B -ntp jacoco:report jacoco:check spotbugs:check'
            }
            post {
                always {
                    // archive the coverage and SpotBugs reports
                    archiveArtifacts artifacts: 'target/site/**', fingerprint: false
                }
            }
        }

        stage('Package') {
            steps {
                // package the app (tests already ran in previous stage)
                sh 'mvn -B -ntp -DskipTests package'

                // archive the jar and make it downloadable from Jenkins
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            steps {
                // Install docker client and curl inside the Maven container (Alpine)
                sh 'apk update && apk add --no-cache docker-cli curl'

                sh 'docker version'

                sh "docker build -t demo-app:${env.BUILD_NUMBER} ."
                sh "docker tag demo-app:${env.BUILD_NUMBER} demo-app:latest"
            }
        }

        stage('Smoke Test') {
            steps {
                script {
                    echo "Cleaning up any previous smoke test container..."
                    sh "docker stop demo-smoke-test || true"
                    sh "docker rm -f demo-smoke-test || true"

                    echo "Running container for smoke test on host port 18080..."
                    sh "docker run -d --rm -p 18080:8080 --name demo-smoke-test demo-app:${env.BUILD_NUMBER}"

                    echo "Waiting for app to start..."
                    sh "sleep 8"

                    echo "Checking /api/products endpoint..."
                    sh """
                        STATUS=\$(curl -s -o /dev/null -w '%{http_code}' http://localhost:18080/api/products || true)
                        if [ "\$STATUS" != "200" ]; then
                            echo "Smoke test failed for /api/products. HTTP status: \$STATUS"
                            exit 1
                        fi
                    """
                }
            }
            post {
                always {
                    echo "Stopping test container..."
                    sh "docker stop demo-smoke-test || true"
                }
            }
        }



        stage('Info') {
            steps {
                echo "Git metadata for this build:"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Commit: ${env.GIT_COMMIT}"
                echo "Docker image tags:"
                echo "  demo-app:${env.BUILD_NUMBER}"
                echo "  demo-app:latest"
            }
        }
    }
}
