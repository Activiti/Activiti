# Activiti HAL Rest Sample Using Spring Boot

This sample program demonstrates the use of a HAL REST API for the Activiti BPM Engine.

The REST endpoints are also secured using keycloak as an identity provider.

## Assumed Keycloak Setup

The keycloak setup used by this example can be replicated by importing the provided keycloak realm json file. The user 'testuser' with password 'password' is used for accessing endpoints. The user 'hr'/'password' is in the 'hr' group. The user 'client'/'client' is for using admin client to look up groups.

To run using a standalone keycloak, download keycloak and run using the following from the keycloak bin directory - ./standalone.sh -Djboss.socket.binding.port-offset=100

The port-offset is important as otherwise Activiti and Keycloak will have a port confllict.

## How to run

To run the sample, run from IDE using the Application.java file but first ensure you have keycloak and rabbitmq docker containers running (see below). To hit an endpoint in the browser, go to http://localhost:8080/process-definitions

A reference dockerfile is also provided (separately) which applies the keycloak json configuration file for the realm to the jboss/keycloak:3.2.0.Final image.

To run it first have docker installed then go to the docker directory and do 'docker build . -t activiti-keycloak' Then execute 'docker run -p 8180:8080 --name keycloak -i -t activiti-keycloak'

If you get a container already in use error when running the docker container then do a docker rm with the id of the running container and try again. To access the admin console for the container go to http://localhost:8180/auth/admin/springboot/console/ and log in as admin/admin. (This reference docker is based upon reference docker is based upon https://github.com/dfranssen/docker-keycloak-import-realm. Note that the admin user has been included in the springboot-realm.json.)

To run the rabbitmq container to go the docker directory and run docker-compose up.

## Postman

A postman postman collection is provided (src/main/postman) which includes a call to get the keycloak token and use it on subsequent requests (based upon http://xpam.pl/blog/?p=154, http://keycloak-user.88327.x6.nabble.com/keycloak-user-Using-postman-to-test-keycloak-protected-app-td3250.html and http://blog.getpostman.com/2014/01/27/extracting-data-from-responses-and-chaining-requests/) - note that the token does expire so can then be necessary to make the call again.

Endpoints can change so the postman collection can get out of sync with the endpoints provided by activiti-services-rest. If so see that project to update. The keycloak endpoint's format is not expected to change unless keycloak changes it.
