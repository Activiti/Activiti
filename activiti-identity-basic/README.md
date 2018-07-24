# Basic Auth IDM

Identity implementation using basic auth. Alternative to using a standalone identity management tool like keycloak. To use replace in maven dependency of intended service.

Expects a properties file named user.properties. Example content:

testuser=password,user
hruser=password,user,hrgroup
client=client,user,admin

Note that groups and roles are not distinguished - this is a limitation of this implementation.

An alternative scenario to consider is obtaining users from tomcat-users.xml or a web.xml via container-managed security. This is not covered by this module as it is a different spring security use-case.