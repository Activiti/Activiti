#!/bin/bash
export MAVEN_OPTS="-Xms512M -Xmx1024M -XX:MaxPermSize=128M -noverify -Xdebug -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
cd ..
mvn -PbuildWebappDependencies clean install
STATUS=$?
if [ $STATUS -eq 0 ] 
then
    cd modules/activiti-webapp-explorer2
    mvn -Dfile.encoding=UTF-8 clean package tomcat7:run
else
    echo "Build failure in dependent project. Cannot boot Activiti Explorer."
fi    
