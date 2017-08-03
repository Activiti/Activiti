REM stop if already running
docker-compose down

REM run build to build docker image

mvn clean install -DskipTests

docker-compose up