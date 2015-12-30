#!/bin/sh
echo "Building dependencies"
cd ..
mvn clean install -DskipTests

STATUS=$?
if [ $STATUS -ne 0 ]
then
  echo "Error building v6 dependencies. Exiting."
  exit $?
fi

cd modules/activiti5-engine/
mvn clean install -DskipTests
STATUS=$?
if [ $STATUS -ne 0 ]
then
  echo "Error building v5 dependencies. Exiting."
  exit $?
fi

cd ../..

cd modules/activiti5-compatibility/
mvn clean install

STATUS=$?
if [ $STATUS -ne 0 ]
then
  echo "Error while building activiti5-compatibility. Exiting."
  exit $?
fi

cd ../..

cd modules/activiti5-test
mvn clean install

STATUS=$?
if [ $STATUS -ne 0 ]
then
  echo "Error while building activiti5-test. Exiting."
  exit $?
else
  echo "All Activiti 5 tests succeeded"
fi

cd ../..

cd modules/activiti5-spring
mvn clean install

STATUS=$?
if [ $STATUS -ne 0 ]
then
  echo "Error while building activiti5-spring. Exiting."
  exit $?
fi

cd ../..

cd modules/activiti5-spring-compatibility/
mvn clean install

STATUS=$?
if [ $STATUS -ne 0 ]
then
  echo "Error while building activiti5-spring-compatibility. Exiting."
  exit $?
fi

cd ../..

cd modules/activiti5-spring-test/
mvn clean install

STATUS=$?
if [ $STATUS -ne 0 ]
then
  echo "Error while building activiti5-spring-test. Exiting."
  exit $?
fi

cd ../..

cd modules/activiti-cxf/
mvn clean install

STATUS=$?
if [ $STATUS -ne 0 ]
then
  echo "Error while building activiti-cxf. Exiting."
  exit $?
fi

cd ../..

cd modules/activiti5-cxf-test/
mvn clean install

STATUS=$?
if [ $STATUS -ne 0 ]
then
  echo "Error while building activiti5-cxf-test. Exiting."
  exit $?
fi

cd ../..

cd modules/activiti-camel/
mvn clean install

STATUS=$?
if [ $STATUS -ne 0 ]
then
  echo "Error while building activiti5-camel. Exiting."
  exit $?
fi

cd ../..

cd modules/activiti5-camel-test/
mvn clean install

STATUS=$?
if [ $STATUS -ne 0 ]
then
  echo "Error while building activiti5-camel-test. Exiting."
  exit $?
fi


echo "All good!"
