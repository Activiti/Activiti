#!/bin/bash
cd ..
mvn -T 1C -Pdistro -DskipTests clean install
STATUS=$?
if [ $STATUS -eq 0 ] 
then
    cd modules/activiti-ui
    ./start.sh
else
    echo "Build failure in dependent project. Cannot boot Activiti UI."
fi    