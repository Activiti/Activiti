#!/usr/bin/env bash

export DOCKER_CONTAINER_NAME=$1
export LOGS_ENTRY=$2

if [[ -z "$DOCKER_CONTAINER_NAME" || -z "$LOGS_ENTRY" ]]
then
  echo "Please provide container name and string to wait for in the logs: \"${0##*/} <container-name> <string-to-wait>\""
  exit 1
fi

echo "Waiting for the $DOCKER_CONTAINER_NAME container to start"

if [[ -z  "$(docker ps | grep ${DOCKER_CONTAINER_NAME})" ]]
then
  echo "Container $DOCKER_CONTAINER_NAME is not running"
  exit 1
fi

timeout 2100s grep -q "${LOGS_ENTRY}" <(docker logs -f ${DOCKER_CONTAINER_NAME}) && echo "Found success log entry. Database is ready." && exit 0 \
    || echo "Could not find success log entry. Database may have not fully started." && exit 0