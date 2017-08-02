#!/bin/bash

#stop if already running
docker-compose down

# run build from parent directory to build docker images

cd ..
mvn clean install -DskipTests

# run docker-compose up from this directory

cd docker
docker-compose up
