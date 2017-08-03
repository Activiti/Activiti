#!/bin/bash

./package.sh
java -jar target/emergency-department-1.0-SNAPSHOT.jar --server.port=8080
