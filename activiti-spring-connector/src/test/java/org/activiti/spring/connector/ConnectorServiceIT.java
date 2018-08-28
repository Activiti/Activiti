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

import org.activiti.model.connector.Connector;
import org.activiti.spring.connector.autoconfigure.ConnectorAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConnectorAutoConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:application-single-test.properties")
public class ConnectorServiceIT {

    @Autowired
    private ConnectorService connectorService;

    @Test
    public void connector() throws IOException {

        List<Connector> connectors = connectorService.get();
        assertNotNull(connectors);
        assertEquals(1, connectors.size());
        assertEquals("Name-of-the-connector", connectors.get(0).getName());
        assertEquals("Description of the connector", connectors.get(0).getDescription());
        assertEquals("path to icon", connectors.get(0).getIcon());
        assertEquals(2, connectors.get(0).getActions().size());
        assertEquals("input-variable-name-1", connectors.get(0).getActions().get("actionName1").getInput().get(0).getName());
    }

//    @org.springframework.context.annotation.Configuration
//    @ComponentScan("org.activiti.spring.connector")
//    public static class Configuration {
//
//    }
}
