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
package org.activiti.services.query.rest;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.activiti.services.query.rest.support.TestMvcClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.LinkDiscoverers;
import org.springframework.hateoas.core.JsonPathLinkDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(
	webEnvironment=WebEnvironment.RANDOM_PORT,
	properties={
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.show-sql=true"
	}
)
public class EnableActivitiRestQueryServiceTest {
	
	private static String basePath = "/v1";

	@Autowired
	private WebApplicationContext context;

	@Autowired 
	private LinkDiscoverers discoverers;

	private MockMvc mvc;
	
	private TestMvcClient client;
	
	@SpringBootApplication
	@EnableActivitiRestQueryService
	static class Configuration {
		
		@Bean
		public LinkDiscoverer alpsLinkDiscoverer() {
			return new JsonPathLinkDiscoverer("$.descriptors[?(@.name == '%s')].href",
					MediaType.valueOf("application/alps+json"));
		}
	}
	
	@Before
	public void setUp() {
		this.mvc = MockMvcBuilders.webAppContextSetup(context).build();
		this.client = new TestMvcClient(mvc, discoverers).setBasePath(basePath);
	}
	
	@Test
	public void contextLoads() {
		// should pass
	}

	@Test
	public void exposesAlpsCollectionResourcesWithIdsAndAssociations() throws Exception {

		// Given 
		Link profileLink = client.discoverUnique("profile");
		
		// When
		Link resourceLink = client.discoverUnique(profileLink, "process-instances", MediaType.ALL);

		// Then
		client.follow(resourceLink, RestMediaTypes.ALPS_JSON)//
			.andExpect(jsonPath("$.alps.version").value("1.0"))//
			.andExpect(jsonPath("$.alps.descriptors[*].name", hasItems("process-instances", "process-instance")))
			.andExpect(jsonPath("$.alps.descriptors[0].descriptors[*].name", hasItems("processInstanceId", "tasks", "variables")))
		;

		// When
		resourceLink = client.discoverUnique(profileLink, "tasks", MediaType.ALL);

		// Then
		client.follow(resourceLink, RestMediaTypes.ALPS_JSON)//
			.andExpect(jsonPath("$.alps.version").value("1.0"))//
			.andExpect(jsonPath("$.alps.descriptors[*].name", hasItems("tasks", "task")))
			.andExpect(jsonPath("$.alps.descriptors[0].descriptors[*].name", hasItems("id", "processInstance", "variables")))
		;

		// When
		resourceLink = client.discoverUnique(profileLink, "variables", MediaType.ALL);

		// Then
		client.follow(resourceLink, RestMediaTypes.ALPS_JSON)//
			.andExpect(jsonPath("$.alps.version").value("1.0"))//
			.andExpect(jsonPath("$.alps.descriptors[*].name", hasItems("variables", "variable")))
			.andExpect(jsonPath("$.alps.descriptors[0].descriptors[*].name", hasItems("id", "processInstance", "task")))
		;
		
	}

	@Test
	public void deleteProcessInstancesNotAllowed() throws Exception {
		deleteResourceIsNotAllowed("process-instances");
	}	

	@Test
	public void deleteTasksNotAllowed() throws Exception {
		deleteResourceIsNotAllowed("tasks");
	}	

	@Test
	public void deleteVariablesNotAllowed() throws Exception {
		deleteResourceIsNotAllowed("variables");
	}	
	
	@Test
	public void getPagingAndSortingProcessInstancesIsOK() throws Exception {
		getPagingAndSortingResourceQueryIsOK("process-instances");
	}	

	@Test
	public void getPagingAndSortingTasksIsOK() throws Exception {
		getPagingAndSortingResourceQueryIsOK("tasks");
	}	

	@Test
	public void getPagingAndSortingVariablesIsOK() throws Exception {
		getPagingAndSortingResourceQueryIsOK("variables");
	}
	
	private void getPagingAndSortingResourceQueryIsOK(String collection) throws Exception {
		mvc.perform(get(basePath+"/"+collection))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._links.self.href", endsWith(collection+"{?page,size,sort}")));
	}
	
	private void deleteResourceIsNotAllowed(String resourceRel) throws Exception {
		mvc.perform(delete(basePath+"/"+resourceRel+"/0"))
			.andExpect(status().is(405));
	}
	
}
