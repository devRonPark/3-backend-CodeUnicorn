pipeline {
    agent any
    tools{
        gradle 'Gradle 7.4.1'
    }

    environment {
        imagename = 'xhfkd00/codeunicorn'
        registryCredential = 'dockerhub'
        dockerImage = ''
        SLACK_CHANNEL = '#codeunicorn'
    }

    stages {
        stage('Start'){
            steps{
                slackSend(channel: SLACK_CHANNEL, color: "good", message:"STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
            }
        }
        // git에서 repository clone
        stage('Clone') {
          steps {
            echo 'Clonning Repository'
            git url: 'https://github.com/adapterz/3-backend-CodeUnicorn.git',
              branch: 'main',
              credentialsId: 'guthub_access_token'
            }
            post {
             success {
               echo 'Successfully Cloned Repository'
             }
           	 failure {
               error 'This pipeline stops here...'
             }
          }
        }

        // gradle build
        stage('Bulid Gradle') {
          steps {
            echo 'Bulid Gradle'
            dir('.'){
                sh """
                cp /var/jenkins_home/keystore.p12 /var/jenkins_home/workspace/CodeUnicon/src/main/resources/keystore.p12
                cp /var/jenkins_home/application-prod.yml /var/jenkins_home/workspace/CodeUnicon/src/main/resources/application-prod.yml
                gradle clean build -x test
                """
            }
          }
          post {
            success{
              echo 'Successfully gradle build'
            }
            failure {
              error 'This pipeline stops here...'
            }
          }
        }

        // docker build
        stage('Bulid Docker') {
          agent any
          steps {
            echo 'Bulid Docker'
            script {
            dir('../CodeUnicon'){
            // 생성된 도커 이미지가 있으면 삭제 후 빌드, 아니면 그냥 빌드
            sh """#!/bin/bash
            if [ -z "docker images | grep xhfkd00/codeunicorn:1.0" ]; then
               docker build -t xhfkd00/codeunicorn:1.0 .
            else
               docker rmi xhfkd00/codeunicorn:1.0
               docker build -t xhfkd00/codeunicorn:1.0 .
            fi
            """
            }

            }
          }
          post {
          success{
              echo 'Successfully docker build'
          }
            failure {
              error 'This pipeline stops here...'
            }
          }
        }

        // docker push
        stage('Push Docker') {
          agent any
          steps {
            echo 'Push Docker'
            script {
            dir('../CodeUnicon'){
            docker.withRegistry('', registryCredential) {
                    sh "docker push xhfkd00/codeunicorn:1.0"
                }
              }
            }
          }
          post {
          success{
            echo 'Successfully Push Docker'
          }
            failure {
              error 'This pipeline stops here...'
            }
          }
        }

        // ec2 접속 및 docker pull, run 실행
        stage('SSH SERVER EC2') {
           steps {
              echo 'SSH'
                sshagent(['ubuntu']) {
                sh 'ssh -o StrictHostKeyChecking=no ubuntu@ec2-13-124-26-116.ap-northeast-2.compute.amazonaws.com "sudo /home/ubuntu/continer.sh"' // 서버 스크립트 실행
              }
           }
           post {
            success{
             echo 'Successfully SSH EC2 script'
            }
             failure {
              error 'This pipeline stops here...'
             }
           }
        }

    }
    // 빌드 성공, 실패 여부 젠킨스 발송
    post {
        success{
            slackSend(channel: SLACK_CHANNEL, color: "good", message:"SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }
        failure{
            slackSend(channel: SLACK_CHANNEL, color: "danger", message:"FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }
    }
}
