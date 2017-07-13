# Activiti HAL Rest Sample Using Spring Boot

This sample program demonstrates the use of a HAL REST API for the Activiti BPM Engine.

The REST endpoints are also secured using keycloak as an identity provider.

The keycloak integration for authentication is based upon https://developers.redhat.com/blog/2017/05/25/easily-secure-your-spring-boot-applications-with-keycloak/ 

The keycloak setup used here can be replicated by importing the provided keycloak realm and users json files. The user 'testuser' with password 'password' is used for accessing endpoints. The user 'client'/'client' is for using admin client to look up groups.

The keycloak integration for passing the user on to Activiti is based upon https://dzone.com/articles/easily-secure-your-spring-boot-applications-with-k

The keycloak id, username or email could be used as identifier. This will be taken as user id by Activiti - choice of which to pass on is made in KeycloakActivitiAuthenticationProvider.

As well as securing the endpoints, keycloak is also being used to find groups for a user via a lookup proxy class.

To run using a standalone keycloak, download keycloak and run using the following from the keycloak bin directory - ./standalone.sh -Djboss.socket.binding.port-offset=100

The port-offset is important as otherwise Activiti and Keycloak will have a port confllict.

To hit an endpoint in the browser, go to http://localhost:8080/api/process-definitions


TODO: Would like  to configure postman to get the keycloak token and use it on subsequent requests - see http://xpam.pl/blog/?p=154 , seems like nobody has documented how to do this successfully for keycloak

TODO: Provide a reference a docker image with the keycloak json files applied... See https://github.com/dfranssen/docker-keycloak-import-realm and https://github.com/keycloak/keycloak-test-docker-images/tree/master/keycloak-sssd-integration-tests for info on how to do this.
