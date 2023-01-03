package org.activiti.bpmn.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ServiceTaskTest {

    @Test
    public void hasBoundaryErrorEvents_should_returnTrue_when_hasAnErrorBoundaryEvent() {
        // given
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setBoundaryEvents(createErrorBoundaryEvents());

        // then
        assertThat(serviceTask.hasBoundaryErrorEvents()).isTrue();
    }

    @Test
    public void hasErrorEventDefinition_should_returnFalse_when_hasNoBoundaryEvent() {
        // given
        ServiceTask serviceTask = new ServiceTask();

        // then
        assertThat(serviceTask.hasBoundaryErrorEvents()).isFalse();
    }

    @Test
    public void hasErrorEventDefinition_should_returnFalse_when_doesNotHaveAnErrorBoundaryEvent() {
        // given
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setBoundaryEvents(createNoErrorBoundaryEvents());

        // then
        assertThat(serviceTask.hasBoundaryErrorEvents()).isFalse();
    }

    private List<BoundaryEvent> createErrorBoundaryEvents() {
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        boundaryEvent.setEventDefinitions(Arrays.asList(new ErrorEventDefinition()));

        return Arrays.asList(boundaryEvent);
    }

    private List<BoundaryEvent> createNoErrorBoundaryEvents() {
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        boundaryEvent.setEventDefinitions(Arrays.asList(new MessageEventDefinition()));

        return Arrays.asList(boundaryEvent);
    }
}
