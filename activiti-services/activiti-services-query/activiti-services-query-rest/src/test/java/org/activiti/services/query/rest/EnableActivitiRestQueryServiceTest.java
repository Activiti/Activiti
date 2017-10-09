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
        webEnvironment = WebEnvironment.MOCK,
        properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=true"
        })
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
        String expectedAlpsVersion = "1.0";

        // When
        Link resourceLink = client.discoverUnique(profileLink, "process-instances", MediaType.ALL);

        // Then
        client.follow(resourceLink, RestMediaTypes.ALPS_JSON)//
              .andExpect(jsonPath("$.alps.version")
                                                   .value(expectedAlpsVersion))
              .andExpect(jsonPath("$.alps.descriptors[*].name")
                                                               .value(hasItems("process-instances",
                                                                               "process-instance")))
              .andExpect(jsonPath("$.alps.descriptors[0].descriptors[*].name")
                                                                              .value(hasItems("processInstanceId",
                                                                                              "tasks",
                                                                                              "variables")));

        // When
        resourceLink = client.discoverUnique(profileLink, "tasks", MediaType.ALL);

        // Then
        client.follow(resourceLink, RestMediaTypes.ALPS_JSON)//
              .andExpect(jsonPath("$.alps.version")
                                                   .value(expectedAlpsVersion))
              .andExpect(jsonPath("$.alps.descriptors[*].name")
                                                               .value(hasItems("tasks", "task")))
              .andExpect(jsonPath("$.alps.descriptors[0].descriptors[*].name")
                                                                              .value(hasItems("id",
                                                                                              "processInstance",
                                                                                              "variables")));

        // When
        resourceLink = client.discoverUnique(profileLink, "variables", MediaType.ALL);

        // Then
        client.follow(resourceLink, RestMediaTypes.ALPS_JSON)//
              .andExpect(jsonPath("$.alps.version")
                                                   .value(expectedAlpsVersion))
              .andExpect(jsonPath("$.alps.descriptors[*].name")
                                                               .value(hasItems("variables", "variable")))
              .andExpect(jsonPath("$.alps.descriptors[0].descriptors[*].name")
                                                                              .value(hasItems("id",
                                                                                              "processInstance",
                                                                                              "task")));

    }

    @Test
    public void deleteProcessInstancesNotAllowed() throws Exception {
        assertDeleteResourceIsNotAllowed("process-instances");
    }

    @Test
    public void deleteTasksNotAllowed() throws Exception {
        assertDeleteResourceIsNotAllowed("tasks");
    }

    @Test
    public void deleteVariablesNotAllowed() throws Exception {
        assertDeleteResourceIsNotAllowed("variables");
    }

    @Test
    public void getPagingAndSortingProcessInstancesIsOK() throws Exception {
        assertGetPagingAndSortingResourceQueryIsOK("process-instances");
    }

    @Test
    public void getPagingAndSortingTasksIsOK() throws Exception {
        assertGetPagingAndSortingResourceQueryIsOK("tasks");
    }

    @Test
    public void getPagingAndSortingVariablesIsOK() throws Exception {
        assertGetPagingAndSortingResourceQueryIsOK("variables");
    }

    // Assert the response status code is HttpStatus.OK (200) and collection is paged and sorted.
    private void assertGetPagingAndSortingResourceQueryIsOK(String collection) throws Exception {
        mvc.perform(get(basePath + "/" + collection))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._links.self.href", endsWith(collection + "{?page,size,sort}")));
    }

    // Assert the response status code is HttpStatus.METHOD_NOT_ALLOWED (405).
    private void assertDeleteResourceIsNotAllowed(String resourceRel) throws Exception {
        mvc.perform(delete(basePath + "/" + resourceRel + "/0"))
            .andExpect(status().isMethodNotAllowed());
    }

}
