FROM jamesdbloom/docker-java7-maven
MAINTAINER Saeid Mirzaei <smirzai@gmail.com>
RUN git clone https://github.com/Activiti/Activiti.git
#RUN cd Activiti/scripts && sed -i  "s/-javaagen..*jar//g" start-explorer.sh
#RUN cd Activiti/scripts && sed -i  "s/clean package/package/g" start-explorer.sh
RUN mvn -f Activiti/pom.xml -PbuildWebappDependencies clean install
RUN mvn -f Activiti/modules/activiti-webapp-explorer2/pom.xml install -DskipTests
RUN mvn -f /local/git/Activiti/modules/activiti-webapp-explorer2/pom.xml  tomcat7:help
CMD mvn -f /local/git/Activiti/modules/activiti-webapp-explorer2/pom.xml  tomcat7:run


EXPOSE 8080
