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
