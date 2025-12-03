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
                sh 'mvn -B clean compile'
            }
        }

        stage('Test') {
            steps {
                // compile the project (no tests here)
                sh 'mvn -B test'
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
                // Run converage + Spotbugs
                sh 'mvn -B jacoco:report spotbugs:check'
            }
            post {
                always {
                    // archive the converage and SpotBugs reports
                    archiveArtifacts artifacts: 'target/site/**', fingerprint: false
                }
            }
        }

        stage('Package') {
            steps {
                // package the app (tests already ran in previous stage)
                sh 'mvn -B -DskipTests package'

                // archive the jar and make it downloadable from Jenkins
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }
}
