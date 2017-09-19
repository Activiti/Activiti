/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.services.query.graphql.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.activiti.services.query.qraphql.autoconfigure.EnableActivitiGraphQLQueryService;
import org.activiti.services.query.qraphql.web.ActivitiGraphQLController.GraphQLQueryRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import graphql.ExecutionResult;
import graphql.GraphQLError;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ActivitiGraphQLControllerIT {
	private static final String	TASK_NAME	= "task1";

	@Autowired
	private TestRestTemplate			rest;
    
    @SpringBootApplication
    @EnableActivitiGraphQLQueryService
    static class Application {
        // Nothing
    }
    
	@Test
	public void testGraphql() {
		GraphQLQueryRequest query = new GraphQLQueryRequest("{Tasks(where:{name:{EQ: \"" + TASK_NAME + "\"}}){select{id assignee priority}}}");

		ResponseEntity<Result> entity = rest.postForEntity("/graphql", new HttpEntity<>(query), Result.class);
		Assert.assertEquals(entity.toString(), HttpStatus.OK, entity.getStatusCode());

		Result result = entity.getBody();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getErrors().toString(), result.getErrors().isEmpty());
		Assert.assertEquals("{Tasks={select=[{id=1, assignee=assignee, priority=Normal}]}}", result.getData().toString());
	}

	@Test
	public void testGraphqlNesting() {
		GraphQLQueryRequest query = new GraphQLQueryRequest(
				"query {"
				+ "ProcessInstances {"
				+ "    select {"
				+ "      processInstanceId"
				+ "      tasks {"
				+ "        id"
				+ "        name"
				+ "        variables {"
				+ "          name"
				+ "          value"
				+ "        }"
				+ "      }"
				+ "    }"
				+ "  }"
				+ "}");

		ResponseEntity<Result> entity = rest.postForEntity("/graphql", new HttpEntity<>(query), Result.class);
		Assert.assertEquals(entity.toString(), HttpStatus.OK, entity.getStatusCode());

		Result result = entity.getBody();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getErrors().toString(), result.getErrors().isEmpty());
		assertThat(((Map<String, Object>)result.getData()).get("ProcessInstances")).isNotNull();
	}
	
	@Test
	public void testGraphqlArguments() throws JsonParseException, JsonMappingException, IOException {
		GraphQLQueryRequest query = new GraphQLQueryRequest("query TasksQuery($name: String!) {Tasks(where:{name:{EQ: $name}}) {select{id assignee priority}}}") ;
		
		String variables = "{\"name\":\"" + TASK_NAME + "\"}";
		query.setVariables(variables);

		ResponseEntity<Result> entity = rest.postForEntity("/graphql", new HttpEntity<>(query), Result.class);
		Assert.assertEquals(entity.toString(), HttpStatus.OK, entity.getStatusCode());

		Result result = entity.getBody();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getErrors().toString(), result.getErrors().isEmpty());
		Assert.assertEquals("{Tasks={select=[{id=1, assignee=assignee, priority=Normal}]}}", result.getData().toString());
	}
}

class Result implements ExecutionResult {
    private Map<String, Object> data;
    private List<GraphQLError> errors;
    private Map<Object, Object> extensions;

    /**
     * Default
     */
    Result() { }
    
    /**
     * @param data the data to set
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(List<GraphQLError> errors) {
        this.errors = errors;
    }

    /**
     * @param extensions the extensions to set
     */
    public void setExtensions(Map<Object, Object> extensions) {
        this.extensions = extensions;
    }

    @Override
    public <T> T getData() {
        return (T) data;
    }
    @Override
    public List<GraphQLError> getErrors() {
        return errors;
    }
    @Override
    public Map<Object, Object> getExtensions() {
        return extensions;
    }
}
