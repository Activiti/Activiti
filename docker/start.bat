REM stop if already running
docker-compose down

REM run build from parent directory to build docker images

cd ..
mvn clean install -DskipTests

REM run docker-compose up from this directory

cd docker
docker-compose up