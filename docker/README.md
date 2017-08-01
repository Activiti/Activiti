# Docker containers for Activiti

## Run Sample App

To run sample application using docker:

1) Add this entry to your hosts file - 127.0.0.1       activiti-keycloak
2) Run start.sh from this directory
3) Go to http://localhost:8090/v1/process-instances
4) To reach the endpoint you'll need to enter testuser/password at the keycloak prompt
5) To create process instances you'll need to use the postman collection

## Integration Tests

The keycloak and rabbitmq containers are used in integration tests through fabric8 docker maven plugin.

## Running Containers independently

The keycloak directory contains a Dockerfile for keycloak. It uses springboot-realm.json as a config file. See sample project. It can be run in that directory using  'docker build . -t activitikeycloak' Then execute 'docker run -p 8180:8080 --name keycloak -i -t activitikeycloak'

rabbitmq directory contains a docker-compose file for rabbitmq. It can be run in that directory using docker-compose up