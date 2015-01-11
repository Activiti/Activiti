#!/bin/sh

echo "Building Activiti engine"
cd ..
mvn -T 1C -Pcheck clean install -DskipTests
cd modules/activiti5-compatibility
mvn -T 1C clean install -DskipTests

echo "Building old engine app project"
cd ../../compatibility-test/old_engine_app/
mvn -T 1C clean package shade:shade
cd target

echo "Running old engine app"
java -jar old-engine-app.jar

echo "Building new engine app project"
cd ../../new_engine_app/
mvn -T 1C clean package shade:shade
cd target

echo "Running new engine app"
java -jar new-engine-app.jar
