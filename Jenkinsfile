pipeline {
    agent any

    tools {
        jdk 'JDK25'
        maven 'Maven3'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '=== Pulling latest code from GitHub ==='
                checkout scm
            }
        }
        stage('Tool Verification') {
            steps {
                echo '=== Verifying Java and Maven ==='
                bat 'java -version'
                bat 'mvn -version'
            }
        }
        stage('Build') {
            steps {
                echo '=== Compiling project (no tests) ==='
                bat 'mvn clean compile -B'
            }
        }
        stage('Test') {
            steps {
                echo '=== Running tests in headless Chrome ==='
                bat 'mvn test -B -Dbrowser=chrome -Dheadless=true'
            }
        }
    }

    post {
        always {
            echo '=== Publishing test reports ==='
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'reports/**/*, target/surefire-reports/**/*, screenshots/**/*',
                             allowEmptyArchive: true
        }
        success { echo 'Build SUCCESS — all tests passed' }
        failure { echo 'Build FAILED — check console output and archived reports' }
        unstable { echo 'Build UNSTABLE — some tests failed' }
    }
}