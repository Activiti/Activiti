/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.core.common.spring.connector;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.activiti.core.common.spring.connector.autoconfigure.ConnectorAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
    classes = ConnectorAutoConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@TestPropertySource(
    locations = "classpath:application-multiple-test.properties"
)
public class MultipleConnectorDefinitionServiceIT {

    @Autowired
    private ConnectorDefinitionService connectorDefinitionService;

    /**
     * Three files have json extensions, one does not.
     **/
    @Test
    public void connectorDefinitions() throws IOException {
        List<ConnectorDefinition> connectorDefinitions = connectorDefinitionService.get();
        assertThat(connectorDefinitions).hasSize(3);
    }
}
