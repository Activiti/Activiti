# Activiti Core Examples

This repository contains Activiti Core Examples using the new ProcessRuntime and TaskRuntime APIs.

- activiti-api-basic-process-example: simple example using the ProcessRuntime APIs only
- activiti-api-basic-task-example: simple example using the TaskRuntime APIs only
- activiti-api-basic-full-example: simple example combining ProcessRuntime and TaskRuntime APIs
- activiti-api-spring-integration-example: simple example combining Spring Integration framework and the new ProcessRuntime APis

These examples are updated regularly to consume our frequent releases which can be found in our Alfresco Nexus Repository: 
```
<repositories>
    <repository>
      <id>activiti-releases</id>
      <url>https://artifacts.alfresco.com/nexus/content/repositories/activiti-releases</url>
    </repository>
</repositories>
```
You can also take a look at our tags for more stable releases such as: 
- [Beta2](https://github.com/Activiti/activiti-examples/blob/7.0.0.Beta2/activiti-api-basic-full-example/pom.xml)

Which uses the Maven Artifacts published in Maven Central. 

For cloud-based examples see https://github.com/Activiti/activiti-cloud-examples

