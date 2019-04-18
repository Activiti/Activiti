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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.activiti.core.common.model.connector.ActionDefinition;
import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectorActionDefinitionFinderTest {

    @Test
    public void findShouldReturnActionMatchingWithImplementation() {
        //given
        ConnectorDefinition firstConnector = buildConnector("firstConnector");
        ConnectorDefinition secondConnector = buildConnector("secondConnector");
        ConnectorDefinition thirdConnector = buildConnector("thirdConnector");

        ActionDefinition firstAction = buildActionDefinition("firstAction");
        ActionDefinition secondAction = buildActionDefinition("secondAction");
        ActionDefinition thirdAction = buildActionDefinition("thirdAction");

        HashMap<String, ActionDefinition> actions = new HashMap<>();
        actions.put(firstAction.getId(), firstAction);
        actions.put(secondAction.getId(), secondAction);
        actions.put(thirdAction.getId(), thirdAction);

        secondConnector.setActions(actions);

        ConnectorActionDefinitionFinder finder = new ConnectorActionDefinitionFinder(Arrays.asList(firstConnector,
                                                                                                                            secondConnector,
                                                                                                                            thirdConnector));

        //when
        ActionDefinition actionDefinition = finder.find("secondConnector.secondAction").orElse(null);

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
        ConnectorActionDefinitionFinder finder = new ConnectorActionDefinitionFinder(Collections.singletonList(connectorDefinition));

        //when
        Optional<ActionDefinition> actionDefinitionOptional = finder.find("does not contain dot");

        //then
        assertThat(actionDefinitionOptional).isNotPresent();
    }
}