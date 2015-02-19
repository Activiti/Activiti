#!/bin/bash
if [[ -z "$TOMCAT_HOME" ]]
then

	echo ""
	echo "Cannot boot Activiti REST web application : TOMCAT_HOME is not set"
    echo ""
	
else

	echo ""
	echo "TOMCAT_HOME = $TOMCAT_HOME"
	echo ""
	
	ORIGINAL_FOLDER=$(pwd)
	export CATALINA_OPTS="-Xms521M -Xmx1024M -noverify -javaagent:/Applications/ZeroTurnaround/JRebel/jrebel.jar -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n"
    cd $TOMCAT_HOME
    rm -rf conf/tomcat-users.xml
    cp $ORIGINAL_FOLDER/tomcat-users.xml conf/
    rm -r webapps/activiti*    
    cd bin
	chmod +x *.sh
    ./catalina.sh run

fi


