package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.junit.jupiter.api.Test;

public class FlowNodeInSubProcessConverterTest extends AbstractConverterTest {

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("subprocess1", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(SubProcess.class);
    SubProcess subProcess = (SubProcess) flowElement;
    ParallelGateway gateway = (ParallelGateway) subProcess.getFlowElement("sid-A0E0B174-36DF-4C4F-A952-311CC3C031FC");
    assertThat(gateway).isNotNull();
    List<SequenceFlow> sequenceFlows = gateway.getOutgoingFlows();
    assertThat(sequenceFlows.size() == 2).isTrue();
    assertThat(sequenceFlows.get(0).getId().equals("sid-9C669980-C274-4A48-BF7F-B9C5CA577DD2") || sequenceFlows.get(0).getId().equals("sid-A299B987-396F-46CA-8D63-85991FBFCE6E")).isTrue();
    assertThat(sequenceFlows.get(1).getId().equals("sid-9C669980-C274-4A48-BF7F-B9C5CA577DD2") || sequenceFlows.get(1).getId().equals("sid-A299B987-396F-46CA-8D63-85991FBFCE6E")).isTrue();
    assertThat(sequenceFlows.get(0).getSourceRef().equals("sid-A0E0B174-36DF-4C4F-A952-311CC3C031FC")).isTrue();
    assertThat(sequenceFlows.get(1).getSourceRef().equals("sid-A0E0B174-36DF-4C4F-A952-311CC3C031FC")).isTrue();
  }

  protected String getResource() {
    return "test.flownodeinsubprocessmodel.json";
  }

}
