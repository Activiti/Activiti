# Activiti HAL Rest Sample Using Spring Boot

This sample program demonstrates the use of a HAL REST API for the Activiti BPM Engine.

The REST endpoints are also secured using keycloak as an identity provider.

## Quickstart

1) Check out the equivalent branch of the main Activiti project and run mvn clean install -DskipTests
2) Add this entry to your hosts file - 127.0.0.1       activiti-keycloak
3) Run start.sh or start.bat from this directory
4) Go to http://localhost:8080/my-activiti-app/v1/process-instances in browser or postman. If using postman collection hit keycloak token endpoint (on host activiti-keycloak) first.
5) For browser to reach the endpoint you'll need to enter testuser/password at the keycloak prompt
6) To create process instances you'll need to use the postman collection

## Gateway and Service Registration

The project uses spring cloud to provide a gateway and service registration.

To see registered services, go to the registry service (by default on http://localhost:8761/). To see gateway routes to services, go to the gateway routes endpoint (should be configured as 8080/application/routes in the Activiti project).

## Alternative Setups

It is also possible to just start the core services directly from the main Activiti project and then start only this sample application using the IDE. To do this rabbitmq also needs to be added to hosts file or changed in application.properties to localhost.

It is also possible to run keycloak and rabbitmq standalone. See the main Activiti project's docker notes for this.

## Postman

A postman postman collection is provided (src/main/postman) which includes a call to get the keycloak token and use it on subsequent requests (based upon http://xpam.pl/blog/?p=154, http://keycloak-user.88327.x6.nabble.com/keycloak-user-Using-postman-to-test-keycloak-protected-app-td3250.html and http://blog.getpostman.com/2014/01/27/extracting-data-from-responses-and-chaining-requests/) - note that the token does expire so can then be necessary to make the call again.

Endpoints can change so the postman collection can get out of sync with the endpoints provided by activiti-services-rest. If so see that project to update. The keycloak endpoint's format is not expected to change unless keycloak changes it.
