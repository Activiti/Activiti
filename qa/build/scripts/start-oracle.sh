#!/usr/bin/env bash

export DOCKER_CONTAINER_NAME=$1
export ORACLE_VERSION=$2
export DOCKER_IMAGE_URL=${3:-"quay.io/alfresco/oracle-database"}


if [[ -z "$DOCKER_CONTAINER_NAME" || -z "$ORACLE_VERSION" ]]
then
  echo "Please provide container name and Oracle DB version: \"${0##*/} <container-name> <oracle-version>\""
  exit 1
fi

docker run -d -p 1521:1521 -e ORACLE_SID=system -e ORACLE_PDB=XE -e ORACLE_PWD=alfresco -e ORACLE_CHARACTERSET=UTF8 --name ${DOCKER_CONTAINER_NAME} ${DOCKER_IMAGE_URL}:${ORACLE_VERSION}

source "$(dirname "$(which "$0")")"/wait-docker-container.sh ${DOCKER_CONTAINER_NAME} "100% complete"