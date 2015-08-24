#!/bin/sh
echo "Building dependencies" 
cd ..
mvn clean install -DskipTests

cd modules/activiti5-engine/
mvn clean install -DskipTests
cd ../..

cd modules/activiti5-compatibility/
mvn clean install -DskipTests
cd ../..

echo "Building test data generators"
cd modules/activiti5-test
mvn clean install
