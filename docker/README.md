# Docker containers for Activiti

## Run Core Engine Services

To use keycloak docker the entry 127.0.0.1       activiti-keycloak will be needed in hosts file.

To run core engine services (inc keycloak as IDM) using docker then run the start.sh script.

## Run a Sample Application

See the spring boot sample app in the example repository on how to run a sample application in which all the core services are used and the engine can be interacted with using REST.

## Integration Tests

The keycloak and rabbitmq containers are used in integration tests through fabric8 docker maven plugin.

In tests that start the containers independently localhost is used in .properties files instead of container names since container names only apply when a docker-compose is used.

## Running Containers independently

The keycloak directory contains a Dockerfile for keycloak. It uses springboot-realm.json as a config file. See sample project. It can be run in that directory using  'docker build . -t activiti-keycloak' Then execute 'docker run -p 8180:8180 --name keycloak -i -t activiti-keycloak'

rabbitmq directory contains a docker-compose file for rabbitmq. It can be run in that directory using docker-compose up

If the containers are run independently then docker names from the docker-compose need to be replaced in property files with localhost or the hosts file updated.

## Replacing Individual Services

The keycloak setup used by this example can be replicated by importing the provided keycloak realm json file. The user 'testuser' with password 'password' is used for accessing endpoints. The user 'hr'/'password' is in the 'hr' group. The user 'client'/'client' is for using admin client to look up groups.

To run using a standalone keycloak, download keycloak and run using the following from the keycloak bin directory - ./standalone.sh 

Rabbitmq can also be run standalone. If this is done then the hostnames referring to 'activiti-keycloak' and 'rabbitmq' need to be replaced with localhost (or host file modified) as those are docker names.