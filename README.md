# activiti-examples

This repository contains Activiti 7.x Examples based on a single java runtime. For cloud-based examples see https://github.com/Activiti/activiti-cloud-examples

## emergency-call-center

This is a demo of using the engine to handle responses to a emergency calls.

## loan-request

Illustration of using the activiti-spring integration to use spring (not spring boot). 

## Note on Spring Boot 

For spring boot use activiti-cloud-starter-runtime-bundle if cloud (includes integration with other cloud components) or activiti-cloud-starter-configure if not cloud. The starter activiti-cloud-starter-configure replaces spring-boot-starter-basic - spring boot usage should otherwise be as per v6. For v6 explanation and examples see:
 
http://www.baeldung.com/spring-activiti
https://spring.io/blog/2015/03/08/getting-started-with-activiti-and-spring-boot
https://github.com/Activiti/Activiti/tree/6.0-release/modules/activiti-spring-boot/spring-boot-samples