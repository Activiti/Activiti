#!/bin/bash

#stop if already running
docker-compose down

# run build to build docker image

mvn clean install -DskipTests

docker-compose up
