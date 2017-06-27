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

package org.activiti;

import org.activiti.rest.SimpleRestTemplateBuilder;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseRestIT {

    @Autowired
    private SimpleRestTemplateBuilder simpleRestTemplateBuilder;

    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int serverPort;

    @Before
    public void setUp() throws Exception {
        restTemplate = simpleRestTemplateBuilder.build(getServerUrl());
    }

    private String getServerUrl() {
        return "http://localhost:" + serverPort;
    }

    protected TestRestTemplate getRestTemplate() {
        return restTemplate;
    }

}
