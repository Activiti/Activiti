# Activiti HAL Rest Sample Using Spring Boot

This sample program demonstrates the use of a HAL REST API for the Activiti BPM Engine.

The REST endpoints are also secured using keycloak as an identity provider.

The keycloak integration for authentication is based upon https://developers.redhat.com/blog/2017/05/25/easily-secure-your-spring-boot-applications-with-keycloak/ 

The keycloak integration for passing the user on to Activiti is based upon https://dzone.com/articles/easily-secure-your-spring-boot-applications-with-k

As well as securing the endpoints, keycloak is also being used to find groups for a user via a lookup proxy class.

TODO: Provide a keycloak json file to configure realm etc, reference a docker image with it applied or both...

TODO: Breakpoint BasicAuthenticationProvider to see whether authentication.getName() contains a keycloak unique id or name or how to ensure it's configurable...