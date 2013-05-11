#!/bin/bash

ORIGINAL_FOLDER=$(pwd)
export MAVEN_OPTS="-Xms521M -Xmx1024M -noverify"
cd ../modules/activiti-webapp-angular
mvn tomcat7:undeploy
mvn clean assembly:assembly tomcat7:deploy   
