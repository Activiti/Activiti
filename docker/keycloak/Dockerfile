FROM jboss/keycloak:3.2.0.Final
MAINTAINER https://gitter.im/Activiti/Activiti7

RUN /opt/jboss/keycloak/bin/add-user.sh -u admin -p admin

ADD springboot-realm.json /opt/jboss/keycloak/

ENTRYPOINT [ "/opt/jboss/docker-entrypoint.sh" ]

CMD ["-b", "0.0.0.0", "-Dkeycloak.import=/opt/jboss/keycloak/springboot-realm.json -Djboss.socket.binding.port-offset=100"]
