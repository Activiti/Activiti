/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.bpmn.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class StartEventTest {

    @Test
    public void getErrorEventDefinition_should_returnEmpty_when_eventDefinitionsIsNull() {
        StartEvent startEvent = new StartEvent();
        startEvent.setEventDefinitions(null);

        Optional<ErrorEventDefinition> result = startEvent.getErrorEventDefinition();

        assertThat(result).isEmpty();
    }

    @Test
    public void getErrorEventDefinition_should_returnEmpty_when_eventDefinitionsIsEmpty() {
        StartEvent startEvent = new StartEvent();
        startEvent.setEventDefinitions(Collections.emptyList());

        Optional<ErrorEventDefinition> result = startEvent.getErrorEventDefinition();

        assertThat(result).isEmpty();
    }

    @Test
    public void getErrorEventDefinition_should_returnErrorEventDefinition_when_firstOfMultipleEventDefinitionsIsErrorEvent() {
        StartEvent startEvent = new StartEvent();
        ErrorEventDefinition errorEventDefinition = new ErrorEventDefinition();
        MessageEventDefinition messageEventDefinition = new MessageEventDefinition();
        startEvent.setEventDefinitions(List.of(errorEventDefinition, messageEventDefinition));

        Optional<ErrorEventDefinition> result = startEvent.getErrorEventDefinition();

        assertThat(result).containsSame(errorEventDefinition);
    }

    @Test
    public void getErrorEventDefinition_should_returnEmpty_when_errorEventDefinitionIsNotFirst() {
        StartEvent startEvent = new StartEvent();
        MessageEventDefinition messageEventDefinition = new MessageEventDefinition();
        ErrorEventDefinition errorEventDefinition = new ErrorEventDefinition();
        startEvent.setEventDefinitions(List.of(messageEventDefinition, errorEventDefinition));

        Optional<ErrorEventDefinition> result = startEvent.getErrorEventDefinition();

        assertThat(result).isEmpty();
    }
}
