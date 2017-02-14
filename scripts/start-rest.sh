#!/bin/bash
export MAVEN_OPTS="-Xms521M -Xmx1024M -XX:MaxPermSize=256M -noverify -javaagent:/Applications/ZeroTurnaround/JRebel/jrebel.jar -Xdebug -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=n"
cd ..
mvn -T 1C -PbuildRestappDependencies clean install
STATUS=$?
if [ $STATUS -eq 0 ] 
then
    cd modules/activiti-webapp-rest2
    mvn -Dfile.encoding=UTF-8 clean package tomcat7:run
else
    echo "Build failure in dependent project. Cannot boot Activiti Rest."
fi    