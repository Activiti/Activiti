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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class BoundaryEventTest {

    @Test
    public void hasErrorEventDefinition_should_returnTrue_when_hasAnErrorEvent() {
        // given
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        boundaryEvent.setEventDefinitions(Arrays.asList(new ErrorEventDefinition()));

        // then
        assertThat(boundaryEvent.hasErrorEventDefinition()).isTrue();
    }

    @Test
    public void hasErrorEventDefinition_should_returnFalse_when_empty() {
        // given
        BoundaryEvent boundaryEvent = new BoundaryEvent();

        // then
        assertThat(boundaryEvent.hasErrorEventDefinition()).isFalse();
    }

    @Test
    public void hasErrorEventDefinition_should_returnFalse_when_doesNotContainErrorEvent() {
        // given
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        boundaryEvent.setEventDefinitions(Arrays.asList(new MessageEventDefinition()));

        // then
        assertThat(boundaryEvent.hasErrorEventDefinition()).isFalse();
    }

    @Test
    public void getErrorEventDefinition_should_returnErrorEventDefinition_when_firstEventDefinitionIsErrorEvent() {
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        ErrorEventDefinition errorEventDefinition = new ErrorEventDefinition();
        boundaryEvent.setEventDefinitions(List.of(errorEventDefinition));

        Optional<ErrorEventDefinition> result = boundaryEvent.getErrorEventDefinition();

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(errorEventDefinition);
    }

    @Test
    public void getErrorEventDefinition_should_returnEmpty_when_eventDefinitionsIsNull() {
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        boundaryEvent.setEventDefinitions(null);

        Optional<ErrorEventDefinition> result = boundaryEvent.getErrorEventDefinition();

        assertThat(result).isEmpty();
    }

    @Test
    public void getErrorEventDefinition_should_returnEmpty_when_eventDefinitionsIsEmpty() {
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        boundaryEvent.setEventDefinitions(Collections.emptyList());

        Optional<ErrorEventDefinition> result = boundaryEvent.getErrorEventDefinition();

        assertThat(result).isEmpty();
    }

    @Test
    public void getErrorEventDefinition_should_returnEmpty_when_firstEventDefinitionIsNotErrorEvent() {
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        MessageEventDefinition messageEventDefinition = new MessageEventDefinition();
        boundaryEvent.setEventDefinitions(List.of(messageEventDefinition));

        Optional<ErrorEventDefinition> result = boundaryEvent.getErrorEventDefinition();

        assertThat(result).isEmpty();
    }

    @Test
    public void getErrorEventDefinition_should_returnErrorEventDefinition_when_firstOfMultipleEventDefinitionsIsErrorEvent() {
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        ErrorEventDefinition errorEventDefinition = new ErrorEventDefinition();
        MessageEventDefinition messageEventDefinition = new MessageEventDefinition();
        boundaryEvent.setEventDefinitions(List.of(errorEventDefinition, messageEventDefinition));

        Optional<ErrorEventDefinition> result = boundaryEvent.getErrorEventDefinition();

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(errorEventDefinition);
    }

    @Test
    public void getErrorEventDefinition_should_returnEmpty_when_errorEventDefinitionIsNotFirst() {
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        MessageEventDefinition messageEventDefinition = new MessageEventDefinition();
        ErrorEventDefinition errorEventDefinition = new ErrorEventDefinition();
        boundaryEvent.setEventDefinitions(List.of(messageEventDefinition, errorEventDefinition));

        Optional<ErrorEventDefinition> result = boundaryEvent.getErrorEventDefinition();

        assertThat(result).isEmpty();
    }
}
