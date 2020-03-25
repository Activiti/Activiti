#!/usr/bin/env bash

export DOCKER_CONTAINER_NAME=$1

echo "killing docker container..."

if [[ -z "$DOCKER_CONTAINER_NAME" ]]
then
  echo "Please provide container name to kill: \"${0##*/} <container-name>\""
  exit 1
fi

docker ps

docker kill ${DOCKER_CONTAINER_NAME}
docker rm ${DOCKER_CONTAINER_NAME}