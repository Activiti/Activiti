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

package org.activiti.services.audit;

import org.activiti.services.audit.events.ProcessEngineEventEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static org.activiti.test.Assertions.assertThat;

@Component
public class EventsRestTemplate {

    private static final String RELATIVE_EVENTS_ENDPOINT = "/v1/events/";

    @Autowired
    private TestRestTemplate restTemplate;

    public ResponseEntity<PagedResources<ProcessEngineEventEntity>> executeFindAll() {
        ResponseEntity<PagedResources<ProcessEngineEventEntity>> eventsResponse = restTemplate.exchange(RELATIVE_EVENTS_ENDPOINT,
                                                                                                  HttpMethod.GET,
                                                                                                  null,
                                                                                                  new ParameterizedTypeReference<PagedResources<ProcessEngineEventEntity>>() {
                                                                                                  });
        assertThat(eventsResponse).hasStatusCode(HttpStatus.OK);
        return eventsResponse;
    }

    public ResponseEntity<ProcessEngineEventEntity> executeFindById(long id) {
        ResponseEntity<ProcessEngineEventEntity> responseEntity = restTemplate.exchange(RELATIVE_EVENTS_ENDPOINT + id,
                                                                                  HttpMethod.GET,
                                                                                  null,
                                                                                  new ParameterizedTypeReference<ProcessEngineEventEntity>() {
                                                                                  });
        assertThat(responseEntity).hasStatusCode(HttpStatus.OK);
        return responseEntity;
    }

}
