FROM jamesdbloom/docker-java7-maven
MAINTAINER Saeid Mirzaei <smirzai@gmail.com>
RUN git clone https://github.com/Activiti/Activiti.git -b activiti-5.19.0
RUN mvn -f Activiti/pom.xml -PbuildWebappDependencies clean install  \
 && mvn -f Activiti/modules/activiti-webapp-explorer2/pom.xml install -DskipTests \
 && mvn -f /local/git/Activiti/modules/activiti-webapp-explorer2/pom.xml  tomcat7:help
RUN  printf '#!/bin/bash\nmvn -f /local/git/Activiti/modules/activiti-webapp-explorer2/pom.xml  tomcat7:run\n' > /local/git/Activiti/explorer \
 &&  printf '#!/bin/bash\nmvn -f /local/git/Activiti/modules/activiti-webapp-rest2/pom.xml  tomcat7:run\n' > /local/git/Activiti/rest \
 && chmod +x /local/git/Activiti/rest  \
 && chmod +x /local/git/Activiti/explorer \
 && echo PATH=$PATH:/local/git/Activiti > ~/.profile
ENV  PATH $PATH:/local/git/Activiti
WORKDIR /local/git/Activiti
CMD mvn -f /local/git/Activiti/modules/activiti-webapp-explorer2/pom.xml  tomcat7:run


EXPOSE 8080
