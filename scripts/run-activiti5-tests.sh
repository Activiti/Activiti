#!/bin/sh
echo "Building dependencies" 
cd ..
mvn clean install -DskipTests

cd modules/activiti5-engine/
mvn clean install -DskipTests
cd ../..

cd modules/activiti5-compatibility/
mvn clean install
cd ../..

cd modules/activiti5-test
mvn clean install
cd ../..

cd modules/activiti5-spring
mvn clean install
cd ../..

cd modules/activiti5-spring-compatibility/
mvn clean install
cd ../..

cd modules/activiti5-spring-test/
mvn clean install
cd ../..

cd modules/activiti-cxf/
mvn clean install
cd ../..

cd modules/activiti5-cxf-test/
mvn clean install
cd ../..

cd modules/activiti-camel/
mvn clean install
cd ../..

cd modules/activiti5-camel-test/
mvn clean install
