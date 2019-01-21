pipeline {
    agent {
      label "jenkins-maven-java11"
    }
    environment {
      ORG               = 'activiti'
      APP_NAME          = 'activiti-examples'
      CHARTMUSEUM_CREDS = credentials('jenkins-x-chartmuseum')
    }
    stages {
      stage('CI Build and push snapshot') {
        when {
          branch 'PR-*'
        }
        steps {
          container('maven') {
            sh "mvn clean verify"
          }
        }
      }
      stage('Build Release') {
        when {
          branch 'develop'
        }
        steps {
          container('maven') {
            // ensure we're not on a detached head
            sh "git checkout develop" 
            sh "git config --global credential.helper store"

            sh "jx step git credentials"
            // so we can retrieve the version in later steps
            sh "echo \$(jx-release-version) > VERSION"
            sh "mvn versions:set -DnewVersion=\$(cat VERSION)"

            sh 'mvn clean verify'

            sh "git add --all"
            sh "git commit -m \"Release \$(cat VERSION)\" --allow-empty"
            sh "git tag -fa v\$(cat VERSION) -m \"Release version \$(cat VERSION)\""
            sh "git push origin v\$(cat VERSION)"
          }
          container('maven') {
            sh 'mvn clean deploy -DskipTests'

          }
        }
      }
    }
    post {
        always {
            cleanWs()
        }
    }
  }
