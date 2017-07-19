# Query Service

This service provides querying capabilities. It is distinct from the run-time API which is used to perform actions on engine items.

Have the rabbitMQ docker container running to use this. See the activiti-services README on this.

To use the query service start QueryApplication from the IDE. Also start SampleApplication, which is used to enable runtime API.

To start a process submit the relevant post to the sample application. Its logs should show 'started on port(s)' to reveal which port. Use the postman collection called 'rest_calls_postman_collection.json' (elsewhere in Activiti).

After starting a process you can query for tasks by going to http://localhost:55085/query/tasks/ or http://localhost:55085/query/process-instances/ where the port is that from the logs of the QueryApplication startup.

The approach for querying tasks using TaskQueryController is based upon http://www.baeldung.com/rest-api-search-language-spring-data-specifications . It supports searches on multiple criteria such as http://localhost:56448/query/tasks?search=priority:50,status:CREATED

It also supports OR using | but in latest Tomcat it has to be escaped as %7C so use e.g. http://localhost:61598/query/tasks?search=priority:50%7Cstatus:CREATED

It also supports paging - simply add &page=0&size=10 on the end of the URL

For sorting add e.g. &sort=processInstanceId,desc where processInstanceId is one of the attributes

TODO: process instances not yet implemented on the ?search= approach, they're using ProcessInstanceQueryRestResource even though would now be easy to create a ProcessInstanceQueryController. Want to resolve the other points first.
TODO: expand to be able to match to a task or processes VARIABLES, requires elaborating model - can we do it without engine?
TODO: need to provide endpoints to get an individual record, not just list