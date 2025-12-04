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
                // Install docker client inside the Maven container (Alpine)
                sh 'apk update && apk add --no-cache docker-cli'

                // Optional: sanity check
                sh 'docker version'

                // Build Docker image using the Dockerfile in the repo root
                sh "docker build -t demo-app:${env.BUILD_NUMBER} ."

                // Tag a 'latest' version for convenience
                sh "docker tag demo-app:${env.BUILD_NUMBER} demo-app:latest"
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
