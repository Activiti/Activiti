# Query Service

This service provides querying capabilities. It is distinct from the run-time API which is used to perform actions on engine items.

Have the rabbitMQ docker container running to use this. See the activiti-services README on this.

To use the query service start QueryApplication from the IDE. Also start SampleApplication, which is used to enable runtime API.

To start a process submit the relevant post to the sample application. Its logs should show 'started on port(s)' to reveal which port. Use the postman collection called 'rest_calls_postman_collection.json' (elsewhere in Activiti).

The events are emitted by the activiti-services-util-audit-producer project. See e.g. ActivitiStartedEventConverter. 

After starting a process you can query for tasks by going to http://localhost:55085/query/tasks/ or http://localhost:55085/query/process-instances/ where the port is that from the logs of the QueryApplication startup.

The approach for querying tasks using TaskQueryController is based upon http://www.baeldung.com/rest-api-search-language-spring-data-specifications . It supports searches on multiple criteria such as http://localhost:56448/query/tasks?search=priority:50,status:CREATED

It also supports OR using | but in latest Tomcat it has to be escaped as %7C so use e.g. http://localhost:61598/query/tasks?search=priority:50%7Cstatus:CREATED

It also supports paging - simply add &page=0&size=10 on the end of the URL

For sorting add e.g. &sort=processInstanceId,desc where processInstanceId is one of the attributes

This approach can also support > and < operators... but I think expanding it to support matching to e.g. task.variables.value might require custom code to recognise . But it might just work so need to try it.

PROCESSINSTANCE implemented using QuerydslPredicate approach so no 'search' param ... see below

TODO: this approach not working on nested objects e.g. query/tasks?search=variables.name:name , see SearchTermResolver.java and TaskSpecification.java
Actually if we are to go the Specification route it might be better to follow https://github.com/ZhongjunTian/spring-repository-plus

TODO: need to provide endpoints to get an individual record, not just list?
TODO: need examples of date restrictions, probably needs some config (https://stackoverflow.com/questions/35155824/can-spring-data-rests-querydsl-integration-be-used-to-perform-more-complex-quer)

## Alternatively use @QuerydslPredicate instead of Specifications?

Process Instances currently can instead be queried using e.g. /query/processinstances?status=RUNNING&page=0&size=10&sort=lastModified,desc

This is because the implementation for proc inst is instead based upon https://github.com/spring-projects/spring-data-examples/tree/master/web/querydsl
Which is much the same as http://www.baeldung.com/rest-api-search-querydsl-web-in-spring-data-jpa

This approach works with nested objects as can query for variables - so e.g. /query/processinstances?variables.name=bob does not match and /query/processinstances?variables.name=name does (provided 'name' is the name of a variable name)

Seems it could be used for named queries http://dontpanic.42.nl/2011/06/spring-data-jpa-with-querydsl.html
And date ranges https://stackoverflow.com/questions/35155824/can-spring-data-rests-querydsl-integration-be-used-to-perform-more-complex-quer

Or if we wanted to do ranges with > and < we could do this - http://www.baeldung.com/rest-api-search-language-spring-data-querydsl but that means having to have ?search= in the uri

If the Q* classes aren't present in the /target/generated-sources directory then run mvn generate-sources from this project directory

## And/or elastic

OR should we use Elasticsearch?
Boot2 supports latest elastic so maybe try an example but with boot2 dependencies e.g. https://www.mkyong.com/spring-boot/spring-boot-spring-data-elasticsearch-example/
Presumably then the Elasticsearch searching and querying would be available - https://www.elastic.co/guide/en/elasticsearch/reference/current/_the_search_api.html

## TODO Variables

Currently hacking variables just to see what can be done. Should really be consuming VariableCreatedEvent in QueryApplication.java

## Flesh out the API to support all the reasonable kinds of queries needed

This will mostly be a matter of consuming all the relevant events and mapping in all of the entities and attributes needed.