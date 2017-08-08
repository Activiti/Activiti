#!/bin/bash

java -jar ./emergency-department-1.0-SNAPSHOT.jar --server.port=9090 &
java -jar ./emergency-department-1.0-SNAPSHOT.jar --server.port=9091 &
java -jar ./emergency-department-1.0-SNAPSHOT.jar --server.port=9092 &
