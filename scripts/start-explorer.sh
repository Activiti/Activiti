#!/bin/bash
export MAVEN_OPTS="-Xms521M -Xmx1024M -noverify -javaagent:/Users/trademakers/Downloads/jrebel/jrebel.jar -Xdebug -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
cd ..
mvn3 -T 1C -PbuildWebappDependencies clean install
STATUS=$?
if [ $STATUS -eq 0 ] 
then
    cd modules/activiti-webapp-explorer2
    mvn3 clean package jetty:run
else
    echo "Build failure in dependent project. Cannot boot Activiti Explorer."
fi    