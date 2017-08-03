#!/bin/bash

./package.sh

java -jar target/emergency-department-1.0-SNAPSHOT.jar --server.port=9090 &
java -jar target/emergency-department-1.0-SNAPSHOT.jar --server.port=9091 &
java -jar target/emergency-department-1.0-SNAPSHOT.jar --server.port=9092 &
