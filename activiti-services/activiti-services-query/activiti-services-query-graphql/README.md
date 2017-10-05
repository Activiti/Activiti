# GraphQL Query Service

This service provides querying capabilities using GraphQL Schema generated at runtime from query entity models. It is distinct from the run-time API which is used to perform actions on engine items.

## Approach

The service provides GraphQL query endpoints with flexible criteria search expressions, paging and sorting. So an example query might be e.g. 

    query {
      ProcessInstance(processInstanceId:1) {
        processInstanceId
        tasks {
          id
          name
          assignee
          status
          variables {
            name
            value
          }
        }
      } 
    }

The query result will be in JSON format:

    {
      "ProcessInstance": {
        "processInstanceId": 1,
        "tasks": [
          {
            "id": "4",
            "name": "task4",
            "assignee": "assignee",
            "status": "Running",
            "variables": [
              {
                "name": "variable6",
                "value": "value6"
              },
              {
                "name": "variable5",
                "value": "value5"
              }
            ]
          },
          {
            "id": "5",
            "name": "task5",
            "assignee": "assignee",
            "status": "Completed",
            "variables": []
          }
        ]
      }
    }

It supports querying for nested objects and nested collections by specifying the selection graph of attributes and subgraph of nested entities with their attributes


## Database Support

The implementation is using JPA EntityManager in as agnostic a way as available so that alternative databases could be used. 

The project relies on the open-source `graphql-jpa-query' module that provides configuration, discovery, schema generation, JPA data fetchers with type-safe JPA queries execution.

## Implementation Details

The Activiti GraphQL Query leverages open-source 'graphql-jpa-query' library https://github.com/introproventures/graphql-jpa-query to do the heavy lifting of GraphQL schema generation and query execution. 

The following dependencies are introduced in `activiti-services-query-models` for GraphQL JPA Query Schema documentation support:

    <dependency>
	    <groupId>com.introproventures</groupId>
	    <artifactId>graphql-jpa-query-annotations</artifactId>
    </dependency>

The entity classes in `activiti-services-query-models` have been augmented with @OneToMany and @ManyToOne annotations to be able to generate GraphQL Schema.

The `activiti-services-query-graphql` module uses GraphQL JPA Query Schema Builder module as a dependency.

    <dependency>
	    <groupId>com.introproventures</groupId>
	    <artifactId>graphql-jpa-query-schema</artifactId>
    </dependency>

Spring Boot auto-configuration with application properties support is provided along with '@EnableActivitiGraphQLQueryService' annotation to configure and enable schema builder, query executor, and web controller at runtime.

Schema Documentation
--------------------
GraphQL provides schema documentation support for domain entity model. The GraphQL JPA Query Schema Builder produces descriptions using @GraphQLDescription annotation on entity Java types and fields. These descriptions will show up in the GraphiQL schema browser to help  provide documented API to end-users. See the GraphiQL section below for more details. You can use @GraphQLIgnore annotation to exclude entity type or field from schema.

Type Safe Arguments
-------------------
The JPA Schema builder will derive QraphQL scalar types from JPA model attributes. At runtime, it will validate provided values against the schema. Enum Java types are also translated to QraphQL Enum scalar type.

Queries
--------------
The schema builder will wrap each discovered entity into two query fields for each entity model, i.e. ProcessInstance or Task entity will have two representations in the generated schema:

- One that models the Entity directly using singular form, i.e. ProcessInstance or Task to query single instance by id.
- One that wraps the Entity in a pagable query request with where criteria expression using Entity pluralized form, i.e. ProcessInstances or Tasks

You can use singular query wrapper, if you need a single object as root of your query. 

For Example:

    query {
      ProcessInstance(processInstanceId:1) {
        processInstanceId
        tasks {
          id
          name
          assignee
          status
        }
      } 
    }

Will return:

    {
      "ProcessInstance": {
        "processInstanceId": 1,
        "tasks": [
          {
            "id": "4",
            "name": "task4",
            "assignee": "assignee",
            "status": "Running"
          },
          {
            "id": "5",
            "name": "task5",
            "assignee": "assignee",
            "status": "Completed"
          }
        ]
      }
    }

Query Wrapper with Where Criteria Expressions
-------------------------------------
This library supports flexible type safe criteria expressions with user-friendly SQL query syntax semantics using `where` arguments and `select` field to specify the entity graph query with entiy attribute names as a combination of logical expressions like OR, AND, EQ, NE, GT, GE, LT, LR, IN, NIN, IS_NULL, NOT_NULL.

For example the following query will find all running process instances with completed tasks: 

    query {
      ProcessInstances(where: {
        OR: {
          status: { LIKE: "Running"}
        }
      }) {
        select {
          processInstanceId
          status
          tasks(where: {status: {EQ: "Completed"}}) {
            id
            name
            assignee
            status
          }
        } 
      }
    }

Will return

    {
      "ProcessInstances": {
        "select": [
          {
            "processInstanceId": 0,
            "status": "Running",
            "tasks": [
              {
                "id": "1",
                "name": "task1",
                "assignee": "assignee",
                "status": "Completed"
              }
            ]
          },
          {
            "processInstanceId": 1,
            "status": "Running",
            "tasks": [
              {
                "id": "5",
                "name": "task5",
                "assignee": "assignee",
                "status": "Completed"
              }
            ]
          }
        ]
      }
    }

Reverse Query
-------------
You can execute an inverse query to fitler results with a join in many-to-one association with some limitations. If you do this, be aware that only static parameter binding are supported in `where` criteria expressions.

For Example: 

    query {
      Tasks {
        select {
          name
          assignee
          status
          processInstance(where: {processInstanceId: {EQ: 1}}) {
            processInstanceId
            status
          }
          variables {
            name
            type
            value
          }
        } 
      }
    }

Will return result:

    {
      "Tasks": {
        "select": [
          {
            "name": "task4",
            "assignee": "assignee",
            "status": "Running",
            "processInstance": {
              "processInstanceId": 1,
              "status": "Running"
            },
            "variables": [
              {
                "name": "variable5",
                "type": "String",
                "value": "value5"
              },
              {
                "name": "variable6",
                "type": "String",
                "value": "value6"
              }
            ]
          },
          {
            "name": "task5",
            "assignee": "assignee",
            "status": "Completed",
            "processInstance": {
              "processInstanceId": 1,
              "status": "Running"
            },
            "variables": []
          }
        ]
      }
    }

Pageable Query Support
-------------------
Use plural query wrapper with Where Criteria Expressions to run complex queries with paged collection results and request total records and pages counts:

    query {
      ProcessInstances(page: {start:1, limit:1}) {
        pages
        total
        select {
          processInstanceId
          tasks {
            id
            name
            assignee
            status
          }
        } 
      }
    }

The result will be:

    {
      "ProcessInstances": {
        "pages": 2,
        "total": 2,
        "select": [
          {
            "processInstanceId": 0,
            "tasks": [
              {
                "id": "2",
                "name": "task2",
                "assignee": "assignee",
                "status": "Running"
              },
              {
                "id": "3",
                "name": "task3",
                "assignee": "assignee",
                "status": "Running"
              },
              {
                "id": "1",
                "name": "task1",
                "assignee": "assignee",
                "status": "Completed"
              }
            ]
          }
        ]
      }
    }
    
Sorting
-------

Sorting is supported on any field.  Simply pass in an 'orderBy' argument with the value of ASC or DESC.  Here's an example
of sorting by name for Task objects. The default sort order can be specified using `GraphQLDefaultSort` annotation on entity field. If sort order is not specified and there is no field with default sort order provided, it will use the field annotated with @Id to avoid paging confusions.

For Example:

    query {
      Tasks {
        select {
          id
          name(orderBy:DESC)
          assignee
          status
        } 
      }
    }
  
Will Return:

    {
      "Tasks": {
        "select": [
          {
            "id": "5",
            "name": "task5",
            "assignee": "assignee",
            "status": "Completed"
          },
          {
            "id": "4",
            "name": "task4",
            "assignee": "assignee",
            "status": "Running"
          },
          {
            "id": "3",
            "name": "task3",
            "assignee": "assignee",
            "status": "Running"
          },
          {
            "id": "2",
            "name": "task2",
            "assignee": "assignee",
            "status": "Running"
          },
          {
            "id": "1",
            "name": "task1",
            "assignee": "assignee",
            "status": "Completed"
          }
        ]
      }
    }

Performance
-----------
The GraphQL JPA Query Data Fetcher implementation will build dynamic fetch graph in order to optimize number of queries executed against database and to avoid N+1 lazy loading problems. 

## How to run the example

Embedded GraphiQL browser (https://github.com/graphql/graphiql) is used for simple testing. It provides schema documentation browser, query builder with auto-completion support, as well as parameter bindings.

Build and run provided Spring Boot Applicatiion class `org.activiti.services.query.graphql.example.Application` from IDE in the `activiti-services-query-graphql' module. 

Navigate to http://localhost:8080/graphiql.html to load GraphiQL browser. The collapsed Docs panel can opened by clicking on the button in the upper right corner to expose current test schema models.

You can run GraphQL queries in the left pannel. Type the query and hit the run button. The results should come up in the middle
panel. If your query has variables, there is a minimized panel at the bottom left.  Simply click on this to expand, and
type in your variables as a JSON string with quoted keys.

## Outstanding

