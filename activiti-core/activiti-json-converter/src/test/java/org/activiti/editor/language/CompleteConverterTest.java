package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

public class CompleteConverterTest extends AbstractConverterTest {

  @Test
  public void convertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  protected String getResource() {
    return "test.completemodel.json";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("userTask1", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement instanceof UserTask).isTrue();
    assertThat(flowElement.getId()).isEqualTo("userTask1");

    flowElement = model.getMainProcess().getFlowElement("catchsignal", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement instanceof IntermediateCatchEvent).isTrue();
    assertThat(flowElement.getId()).isEqualTo("catchsignal");
    IntermediateCatchEvent catchEvent = (IntermediateCatchEvent) flowElement;
    assertThat(catchEvent.getEventDefinitions().size()).isEqualTo(1);
    assertThat(catchEvent.getEventDefinitions().get(0) instanceof SignalEventDefinition).isTrue();
    SignalEventDefinition signalEvent = (SignalEventDefinition) catchEvent.getEventDefinitions().get(0);
    assertThat(signalEvent.getSignalRef()).isEqualTo("testSignal");

    flowElement = model.getMainProcess().getFlowElement("subprocess", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement instanceof SubProcess).isTrue();
    assertThat(flowElement.getId()).isEqualTo("subprocess");
    SubProcess subProcess = (SubProcess) flowElement;

    flowElement = subProcess.getFlowElement("receiveTask");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement instanceof ReceiveTask).isTrue();
    assertThat(flowElement.getId()).isEqualTo("receiveTask");
  }
}
