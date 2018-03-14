FROM maven:3.5-jdk-8
MAINTAINER Saeid Mirzaei <smirzai@gmail.com>
WORKDIR /local/git
RUN git clone https://github.com/Activiti/Activiti.git -b 5.x
RUN mvn -f Activiti/pom.xml -PbuildWebappDependencies,buildRestappDependencies clean install -DskipTests
RUN printf '#!/bin/bash\nmvn -f /local/git/Activiti/modules/activiti-webapp-explorer2/pom.xml tomcat7:run\n' > /local/git/Activiti/explorer \
&& printf '#!/bin/bash\nmvn -f /local/git/Activiti/modules/activiti-webapp-rest2/pom.xml tomcat7:run\n' > /local/git/Activiti/rest \
&& chmod +x /local/git/Activiti/rest \
&& chmod +x /local/git/Activiti/explorer \
&& echo PATH=$PATH:/local/git/Activiti > ~/.profile
ENV PATH $PATH:/local/git/Activiti
CMD mvn -f Activiti/modules/activiti-webapp-explorer2/pom.xml tomcat7:run

EXPOSE 8080
