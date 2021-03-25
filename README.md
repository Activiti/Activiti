Activiti
========

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.activiti/activiti-engine/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.activiti/activiti-engine)

Homepage: http://activiti.org/


Activiti is a light-weight workflow and Business Process Management (BPM) Platform targeted at business people, developers and system admins. Its core is a super-fast and rock-solid BPMN 2 process engine for Java. It's open-source and distributed under the Apache license. Activiti runs in any Java application, on a server, on a cluster or in the cloud. It integrates perfectly with Spring, it is extremely lightweight and based on simple concepts. 

Activiti JIRA: https://activiti.atlassian.net

Activiti QA: http://build.activiti.org:8080

git checkout -b study6 activiti-6.0.0

mvn clean test-compile

mvn versions:set -DnewVersion=6.0.0-boot2

mvn clean install source:jar -Dmaven.test.skip=true

mvn clean tomcat7:run
