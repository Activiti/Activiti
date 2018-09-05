/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.spring.connector;

import java.io.IOException;
import java.util.List;

import org.activiti.model.connector.ConnectorDefinition;
import org.activiti.spring.connector.autoconfigure.ConnectorAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConnectorAutoConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:application-single-test.properties")
public class ConnectorDefinitionServiceIT {

    @Autowired
    private ConnectorService connectorService;

    @Test
    public void connector() throws IOException {

        List<ConnectorDefinition> connectorDefinitions = connectorService.get();
        assertThat(connectorDefinitions).hasSize(1);
        assertThat(connectorDefinitions.get(0).getId()).isEqualTo("connector-uuid");
        assertThat(connectorDefinitions.get(0).getName()).isEqualTo("Name-of-the-connector");
        assertThat(connectorDefinitions.get(0).getActions().size()).isEqualTo(2);
        assertThat(connectorDefinitions.get(0).getActions().get("actionId1").getName()).isEqualTo("actionName1");
        assertThat(connectorDefinitions.get(0).getActions().get("actionId1").getInput().get(0).getName()).isEqualTo("input-variable-name-1");
        assertThat(connectorDefinitions.get(0).getActions().get("actionId1").getOutput().get(0).getName()).isEqualTo("output-variable-name-1");
    }
}
