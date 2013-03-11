#!/bin/bash
export MAVEN_OPTS="-Xms521M -Xmx1024M -noverify -javaagent:/Applications/ZeroTurnaround/JRebel/jrebel.jar -Xdebug -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
cd ..
mvn -T 1C -PbuildWebappDependencies clean install
STATUS=$?
if [ $STATUS -eq 0 ] 
then
    cd modules/activiti-webapp-explorer2
    mvn clean install jetty:run
else
    echo "Build failure in dependent project. Cannot boot Activiti Explorer."
fi    