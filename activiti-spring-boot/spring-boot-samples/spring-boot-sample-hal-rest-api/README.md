# Activiti HAL Rest Sample Using Spring Boot

This sample program demonstrates the use of a HAL REST API for the Activiti BPM Engine.

The REST endpoints are also secured using keycloak as an identity provider.

The keycloak integration for authentication is based upon https://developers.redhat.com/blog/2017/05/25/easily-secure-your-spring-boot-applications-with-keycloak/ 

The keycloak setup used here can be replicated by importing the provided keycloak realm json file. The user 'testuser' with password 'password' is used for accessing endpoints. The user 'hr'/'password' is in the 'hr' group. The user 'client'/'client' is for using admin client to look up groups.

The keycloak integration for passing the user on to Activiti is based upon https://dzone.com/articles/easily-secure-your-spring-boot-applications-with-k

The keycloak id, username or email could be used as identifier. This will be taken as user id by Activiti - choice of which to pass on is made in KeycloakActivitiAuthenticationProvider.

As well as securing the endpoints, keycloak is also being used to find groups for a user via a lookup proxy class.

To run using a standalone keycloak, download keycloak and run using the following from the keycloak bin directory - ./standalone.sh -Djboss.socket.binding.port-offset=100

The port-offset is important as otherwise Activiti and Keycloak will have a port confllict.

To run the sample, run from IDE using the Application.java file. To hit an endpoint in the browser, go to http://localhost:8080/process-definitions

A postman postman collection is provided which includes a call to get the keycloak token and use it on subsequent requests (based upon http://xpam.pl/blog/?p=154, http://keycloak-user.88327.x6.nabble.com/keycloak-user-Using-postman-to-test-keycloak-protected-app-td3250.html and http://blog.getpostman.com/2014/01/27/extracting-data-from-responses-and-chaining-requests/) - note that the token does expire so can then be necessary to make the call again.

A reference dockerfile is also provided which applies the keycloak json configuration file for the realm to the jboss/keycloak:3.2.0.Final image.

To run it first have docker installed then go to the directory and do 'docker build . -t activitikeycloak' Then execute 'docker run -p 8180:8080 --name keycloak -i -t activitikeycloak'

TODO: The reference docker is based upon https://github.com/dfranssen/docker-keycloak-import-realm but creation of admin user isn't working for master and no config of master realm is provided (could add as they can be put on comma-separated on keycloak.import). But the realm we need to run tests is there.
