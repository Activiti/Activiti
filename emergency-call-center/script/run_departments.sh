#!/bin/bash

java -jar ../emergency-department/target/emergency-department-7-201708-EA-SNAPSHOT.jar --server.port=9090 &
java -jar ../emergency-department/target/emergency-department-7-201708-EA-SNAPSHOT.jar --server.port=9091 &
java -jar ../emergency-department/target/emergency-department-7-201708-EA-SNAPSHOT.jar --server.port=9092 &
