export MAVEN_OPTS="$MAVEN_OPTS -Xms512m -Xmx1024m -XX:MaxPermSize=512m -Xdebug -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=9000,server=y,suspend=n"
mvn -Pdev -DskipTests -Dfile.encoding=UTF-8 clean tomcat7:run
