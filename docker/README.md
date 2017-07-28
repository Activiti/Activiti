# Docker containers for Activiti

Dockerfile is for keycloak (which provides identity management). It uses springboot-realm.json as a config file.

docker-compose.yml is for rabbitmq.

Both are extensively used.

## Running Keycloak

Go to the docker directory and do 'docker build . -t activitikeycloak' Then execute 'docker run -p 8180:8080 --name keycloak -i -t activitikeycloak'

If you get a container already in use error when running the docker container then do a docker rm with the id of the running container and try again. To access the admin console for the container go to http://localhost:8180/auth/admin/springboot/console/ and log in as admin/admin. (This reference docker is based upon reference docker is based upon https://github.com/dfranssen/docker-keycloak-import-realm. Note that the admin user has been included in the springboot-realm.json.)

## Running rabbimq

To run the rabbitmq container to go the docker directory and run docker-compose up.