package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.bpmn.model.*;
import org.junit.jupiter.api.Test;

public class AsyncEndEventConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
    validateModel(parsedModel);
    deployProcess(parsedModel);
  }

  protected String getResource() {
    return "asyncendeventmodel.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("endEvent");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(EndEvent.class);
    assertThat(flowElement.getId()).isEqualTo("endEvent");
    EndEvent endEvent = (EndEvent) flowElement;
    assertThat(endEvent.getId()).isEqualTo("endEvent");
    assertThat(endEvent.isAsynchronous()).isTrue();

    List<ActivitiListener> listeners = endEvent.getExecutionListeners();
    assertThat(listeners).hasSize(1);
    ActivitiListener listener = listeners.get(0);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("org.test.TestClass");
    assertThat(listener.getEvent()).isEqualTo("start");

    assertThat(endEvent.getIncomingFlows()).hasSize(1);
    SequenceFlow sequence = endEvent.getIncomingFlows().get(0);
    assertThat(sequence.getId()).isEqualTo("sid-91C0F3A0-649F-462E-A1C1-1CE499FEDE3E");
  }
}
