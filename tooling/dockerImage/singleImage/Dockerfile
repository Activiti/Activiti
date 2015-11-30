FROM jamesdbloom/docker-java7-maven
MAINTAINER Saeid Mirzaei <smirzai@gmail.com>
RUN git clone https://github.com/Activiti/Activiti.git -b activiti-5.19.0
RUN mvn -f Activiti/pom.xml -PbuildWebappDependencies clean install  \
 && mvn -f Activiti/modules/activiti-webapp-explorer2/pom.xml install -DskipTests \
 && mvn -f /local/git/Activiti/modules/activiti-webapp-explorer2/pom.xml  tomcat7:help
CMD mvn -f /local/git/Activiti/modules/activiti-webapp-explorer2/pom.xml  tomcat7:run


EXPOSE 8080
