echo "Dropping DB schema"
mysql -u alfresco -palfresco -e "DROP SCHEMA activitiadmin"

echo "Creating DB schema"
mysql -u alfresco -palfresco -e "CREATE SCHEMA activitiadmin DEFAULT CHARACTER SET utf8 COLLATE utf8_bin"

export MAVEN_OPTS="-noverify -javaagent:/Applications/ZeroTurnaround/jrebel/jrebel.jar -Xms512m -Xmx1024m -XX:MaxPermSize=512m -Xdebug -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=9000,server=y,suspend=n"
mvn -Pdev -DskipTests -Dfile.encoding=UTF-8 clean tomcat7:run
