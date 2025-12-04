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

        stage('Info') {
            steps {
                echo "Git metadata for this build:"
                sh 'git rev-parse HEAD'
                sh 'git rev-parse --abbrev-ref HEAD'
            }
        }
    }
}
