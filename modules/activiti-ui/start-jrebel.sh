#!/bin/sh
echo "Building all submodules"
mvn -T 1C clean install -DskipTests
STATUS=$?
if [ $STATUS -eq 0 ]
then
	cd activiti-app

	# Run war
	echo "Running war file"
	export MAVEN_OPTS="$MAVEN_OPTS -noverify -Xms512m -Xmx1024m -XX:MaxPermSize=512m -javaagent:/Applications/ZeroTurnaround/JRebel/jrebel.jar -Xdebug -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
	mvn clean tomcat7:run
else
	say -v Cellos "Dum dum dum dum dum dum dum he he he ho ho ho fa lah lah lah lah lah lah fa lah full hoo hoo hoo"
    echo "Error while building root pom. Halting."
fi
	
