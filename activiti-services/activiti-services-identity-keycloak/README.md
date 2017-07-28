# Keycloak Integration

This module provides an adapater for keycloak. To see it in use see the spring-boot-sample-hal-rest-api project.

The keycloak integration for authentication is based upon https://developers.redhat.com/blog/2017/05/25/easily-secure-your-spring-boot-applications-with-keycloak/ 

The keycloak integration for passing the user on to Activiti is based upon https://dzone.com/articles/easily-secure-your-spring-boot-applications-with-k

The keycloak id, username or email could be used as identifier. This will be taken as user id by Activiti - choice of which to pass on is made in KeycloakActivitiAuthenticationProvider.

As well as securing the endpoints, keycloak is also being used to find groups for a user via a lookup proxy class.

See docker directory for a docker container.

The implementation is modularised so that another provider could be integrated in a similar fashion by implementing the same interfaces and replacing the relevant dependencies in the starter through maven (exclude and new dependency).