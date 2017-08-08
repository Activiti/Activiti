#!/bin/bash

java -jar target/emergency-department-7-201708-EA-SNAPSHOT.jar --server.port=9090 &
java -jar target/emergency-department-7-201708-EA-SNAPSHOT.jar --server.port=9091 &
java -jar target/emergency-department-7-201708-EA-SNAPSHOT.jar --server.port=9092 &
