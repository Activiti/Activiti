/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.support.ResourcePatternResolver;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(MockitoExtension.class)
public class ConnectorDefinitionServiceTest {

    private ConnectorDefinitionService connectorDefinitionService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ResourcePatternResolver resourceLoader;

    @BeforeEach
    public void setUp() {
        connectorDefinitionService = new ConnectorDefinitionService("/connectors",
                                                                       objectMapper,
                                                                       resourceLoader);
    }

    @Test
    public void validateShouldThrowExceptionWhenConnectorNameIsNull() {
        //given
        ConnectorDefinition connectorDefinition = new ConnectorDefinition();
        connectorDefinition.setName(null);

        //when
        Throwable throwable = catchThrowable(
                () -> connectorDefinitionService.validate(singletonList(connectorDefinition))
        );

        //then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("connectorDefinition name cannot be null or empty");
    }

    @Test
    public void validateShouldThrowExceptionWhenConnectorNameIsEmpty() {
        //given
        ConnectorDefinition connectorDefinition = new ConnectorDefinition();
        connectorDefinition.setName("");

        //when
        Throwable throwable = catchThrowable(
                () -> connectorDefinitionService.validate(singletonList(connectorDefinition))
        );

        //then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("connectorDefinition name cannot be null or empty");
    }

    @Test
    public void validateShouldThrowExceptionWhenConnectorNameContainsDotCharacter() {
        //given
        ConnectorDefinition connectorDefinition = new ConnectorDefinition();
        connectorDefinition.setName("connector.name");

        //when
        Throwable throwable = catchThrowable(
                () -> connectorDefinitionService.validate(singletonList(connectorDefinition))
        );

        //then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("connectorDefinition name cannot have '.' character");
    }

    @Test
    public void validateShouldThrowExceptionWhenTwoConnectorsHaveTheSameName() {
        //given
        ConnectorDefinition connectorDefinition = new ConnectorDefinition();
        connectorDefinition.setName("Conflicting name connector");

        ConnectorDefinition connectorDefinitionWithSameName = new ConnectorDefinition();
        connectorDefinitionWithSameName.setName("Conflicting name connector");

        //when
        Throwable throwable = catchThrowable(() -> connectorDefinitionService.validate(asList(connectorDefinition, connectorDefinitionWithSameName)));

        //then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("More than one connectorDefinition with name '" + connectorDefinition.getName() + "' was found. Names must be unique.");
    }

}
