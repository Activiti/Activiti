#!/usr/bin/env bash
echo "Dropping DB schema"
mysql -u activiti -pactiviti -e "DROP SCHEMA activitiupgrade"

echo "Creating DB schema"
mysql -u activiti -pactiviti -e "CREATE SCHEMA activitiupgrade DEFAULT CHARACTER SET utf8 COLLATE utf8_bin"

echo "Building dependencies"
cd ..
mvn clean install -DskipTests

echo "Building upgrade test data generators"
cd modules/activiti-upgrade-test
mvn clean package shade:shade -DskipTests -Pactiviti5-engine

echo "Generating upgrade test data"
cd target
java -jar activiti-upgrade-test.jar

echo "Running upgrade tests"
cd ../../activiti-upgrade-test
mvn clean test -Pactiviti6-engine