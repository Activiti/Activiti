pipeline {
    agent {
      label "jenkins-maven-java11"
    }
    environment {
      ORG               = 'activiti'
      APP_NAME          = 'activiti-dependencies'
    }
    stages {
      stage('CI Build and push snapshot') {
        when {
          branch 'PR-*'
        }
        environment {
          PREVIEW_VERSION = "0.0.0-SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER"
          PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
          HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
        }
        steps {
          container('maven') {
            sh "mvn versions:set -DnewVersion=$PREVIEW_VERSION"
            sh "mvn install"
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

            sh 'export VERSION=`cat VERSION`'

            sh "git config --global credential.helper store"

            sh "jx step git credentials"
            //sh "updatebot push-version --kind maven org.activiti.dependencies:activiti-dependencies \$(cat VERSION) --merge false"
            sh "make updatebot/push-version"
              
            sh "updatebot update --merge false"

          }
        }
      }
      stage('Build Release from Tag') {
        when {
          tag '*RELEASE'
        }
        steps {
          container('maven') {
            // ensure we're not on a detached head
            sh "git checkout $TAG_NAME"
            sh "git config --global credential.helper store"

            sh "jx step git credentials"
            // so we can retrieve the version in later steps
            sh "echo \$TAG_NAME > VERSION"
            sh "mvn versions:set -DnewVersion=\$(cat VERSION)"
          }
          container('maven') {
            sh '''
              mvn clean deploy -P !alfresco -P central
              '''

            sh 'export VERSION=`cat VERSION`'

            sh "git config --global credential.helper store"

            sh "jx step git credentials"

            sh "echo pushing with update using version \$(cat VERSION)"

            //sh "updatebot push-version --kind maven org.activiti.dependencies:activiti-dependencies \$(cat VERSION)"
            sh "make updatebot/push-version"
          }
        }
      }

    }
    post {
        failure {
           slackSend(
             channel: "#activiti-community-builds",
             color: "danger",
             message: "activiti-dependencies branch=$BRANCH_NAME is failed http://jenkins.jx.35.240.9.95.nip.io/job/Activiti/job/activiti-dependencies/"
           )
        } 
        always {
            cleanWs()
        }
    }
  }
