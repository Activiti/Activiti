/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.connector;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.activiti.core.common.model.connector.ActionDefinition;
import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.activiti.spring.connector.loader.ProcessConnectorService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConnectorActionDefinitionFinderTest {

    @InjectMocks
    private ConnectorActionDefinitionFinder finder;

    @Mock
    private ProcessConnectorService connectorService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void findShouldReturnActionMatchingWithImplementation() {
        //given
        ConnectorDefinition secondConnector = buildConnector("secondConnector");

        ActionDefinition firstAction = buildActionDefinition("firstAction");
        ActionDefinition secondAction = buildActionDefinition("secondAction");
        ActionDefinition thirdAction = buildActionDefinition("thirdAction");

        HashMap<String, ActionDefinition> actions = new HashMap<>();
        actions.put(firstAction.getId(), firstAction);
        actions.put(secondAction.getId(), secondAction);
        actions.put(thirdAction.getId(), thirdAction);

        secondConnector.setActions(actions);

        given(connectorService.findConnector("procDefId", "secondConnector"))
                .willReturn(secondConnector);

        //when
        ActionDefinition actionDefinition = finder.find("procDefId" , "secondConnector.secondAction").orElse(null);

        //then
        assertThat(actionDefinition).isNotNull();
        assertThat(actionDefinition.getName()).isEqualTo("secondAction");
    }

    private ActionDefinition buildActionDefinition(String name) {
        ActionDefinition actionDefinition = new ActionDefinition();
        actionDefinition.setName(name);
        actionDefinition.setId(UUID.randomUUID().toString());
        return actionDefinition;
    }

    private ConnectorDefinition buildConnector(String name) {
        ConnectorDefinition connectorDefinition = new ConnectorDefinition();
        connectorDefinition.setName(name);
        return connectorDefinition;
    }

    @Test
    public void findShouldReturnEmptyOptionalWhenImplementationDoesNotContainDotChar() {
        //given
        ConnectorDefinition connectorDefinition = new ConnectorDefinition();
        connectorDefinition.setName("my connector");

        //when
        Optional<ActionDefinition> actionDefinitionOptional = finder.find("procDefId", "does not contain dot");

        //then
        assertThat(actionDefinitionOptional).isNotPresent();
    }
}