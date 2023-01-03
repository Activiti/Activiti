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

package org.activiti.bpmn.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
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
}
