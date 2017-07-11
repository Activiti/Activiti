/*
 * Copyright 2017 Alfresco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.services.query;

import org.activiti.services.query.events.ProcessEngineEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QueryServiceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String relativeQueryEndpoint = "/api/query/";

    @Test
    public void getAllEventsTests() throws Exception {
        //given
        ResponseEntity<PagedResources<ProcessEngineEvent>> eventsPagedResources = restTemplate.exchange(relativeQueryEndpoint,
                                                                                                        HttpMethod.GET,
                                                                                                        null,
                                                                                                        new ParameterizedTypeReference<PagedResources<ProcessEngineEvent>>() {
                                                                                });
        //then
        assertThat(eventsPagedResources).isNotNull();
        assertThat(eventsPagedResources.getBody().getContent()).hasSize(0);
    }
}
