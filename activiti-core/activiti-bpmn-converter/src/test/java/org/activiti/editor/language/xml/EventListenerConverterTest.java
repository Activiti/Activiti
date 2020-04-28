package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventListener;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Process;
import org.junit.jupiter.api.Test;

/**
 * Test for ACT-1657
 *

 */
public class EventListenerConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  protected String getResource() {
    return "eventlistenersmodel.bpmn20.xml";
  }

  private void validateModel(BpmnModel model) {
    Process process = model.getMainProcess();
    assertThat(process).isNotNull();
    assertThat(process.getEventListeners()).isNotNull();
    assertThat(process.getEventListeners()).hasSize(8);

    // Listener with class
    EventListener listener = process.getEventListeners().get(0);
    assertThat(listener.getEvents()).isEqualTo("ENTITY_CREATE");
    assertThat(listener.getImplementation()).isEqualTo("org.activiti.test.MyListener");
    assertThat(listener.getImplementationType()).isEqualTo(ImplementationType.IMPLEMENTATION_TYPE_CLASS);

    // Listener with class, but no specific event (== all events)
    listener = process.getEventListeners().get(1);
    assertThat(listener.getEvents()).isNull();
    assertThat(listener.getImplementation()).isEqualTo("org.activiti.test.AllEventTypesListener");
    assertThat(listener.getImplementationType()).isEqualTo(ImplementationType.IMPLEMENTATION_TYPE_CLASS);

    // Listener with delegate expression
    listener = process.getEventListeners().get(2);
    assertThat(listener.getEvents()).isEqualTo("ENTITY_DELETE");
    assertThat(listener.getImplementation()).isEqualTo("${myListener}");
    assertThat(listener.getImplementationType()).isEqualTo(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);

    // Listener that throws a signal-event
    listener = process.getEventListeners().get(3);
    assertThat(listener.getEvents()).isEqualTo("ENTITY_DELETE");
    assertThat(listener.getImplementation()).isEqualTo("theSignal");
    assertThat(listener.getImplementationType()).isEqualTo(ImplementationType.IMPLEMENTATION_TYPE_THROW_SIGNAL_EVENT);

    // Listener that throws a global signal-event
    listener = process.getEventListeners().get(4);
    assertThat(listener.getEvents()).isEqualTo("ENTITY_DELETE");
    assertThat(listener.getImplementation()).isEqualTo("theSignal");
    assertThat(listener.getImplementationType()).isEqualTo(ImplementationType.IMPLEMENTATION_TYPE_THROW_GLOBAL_SIGNAL_EVENT);

    // Listener that throws a message-event
    listener = process.getEventListeners().get(5);
    assertThat(listener.getEvents()).isEqualTo("ENTITY_DELETE");
    assertThat(listener.getImplementation()).isEqualTo("theMessage");
    assertThat(listener.getImplementationType()).isEqualTo(ImplementationType.IMPLEMENTATION_TYPE_THROW_MESSAGE_EVENT);

    // Listener that throws an error-event
    listener = process.getEventListeners().get(6);
    assertThat(listener.getEvents()).isEqualTo("ENTITY_DELETE");
    assertThat(listener.getImplementation()).isEqualTo("123");
    assertThat(listener.getImplementationType()).isEqualTo(ImplementationType.IMPLEMENTATION_TYPE_THROW_ERROR_EVENT);

    // Listener restricted to a specific entity
    listener = process.getEventListeners().get(7);
    assertThat(listener.getEvents()).isEqualTo("ENTITY_DELETE");
    assertThat(listener.getImplementation()).isEqualTo("123");
    assertThat(listener.getImplementationType()).isEqualTo(ImplementationType.IMPLEMENTATION_TYPE_THROW_ERROR_EVENT);
    assertThat(listener.getEntityType()).isEqualTo("job");
  }
}
