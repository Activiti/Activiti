# Activiti Services

Contains key Activiti services, each intended to be isolated.

Docker contains a docker-compose which set up a container based upon a rabbitMQ image. Start this by going to the directory and running docker-compose up (docker needs to be installed).

To use the query service start QueryApplication from the IDE. Also start SampleApplication.

To start a process submit the relevant post to the sample application. Its logs should show 'started on port(s)' to reveal which port. Use the postman collection called 'rest_calls_postman_collection.json' (elsewhere in Activiti).

After starting a process you can query for tasks by going to http://localhost:55085/query/tasks/ where the port is that from the logs of the QueryApplication startup.