package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.junit.jupiter.api.Test;

public class FlowNodeMultipleOutgoingFlowsConverterTest extends AbstractConverterTest {

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("parallel1", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(ParallelGateway.class);
    ParallelGateway gateway = (ParallelGateway) flowElement;
    List<SequenceFlow> sequenceFlows = gateway.getOutgoingFlows();
    assertThat(sequenceFlows.size() == 3).isTrue();
    assertThat(sequenceFlows.get(0).getId().equals("sid-B9EE4ECE-BF72-4C25-B768-8295906E5CF8") || sequenceFlows.get(0).getId().equals("sid-D2491B73-0382-4EC2-AAAC-C8FD129E4CBE")
        || sequenceFlows.get(0).getId().equals("sid-7036D56C-E8EF-493B-ADEC-57EED4C6CE1F")).isTrue();
    assertThat(sequenceFlows.get(1).getId().equals("sid-B9EE4ECE-BF72-4C25-B768-8295906E5CF8") || sequenceFlows.get(1).getId().equals("sid-D2491B73-0382-4EC2-AAAC-C8FD129E4CBE")
        || sequenceFlows.get(1).getId().equals("sid-7036D56C-E8EF-493B-ADEC-57EED4C6CE1F")).isTrue();
    assertThat(sequenceFlows.get(2).getId().equals("sid-B9EE4ECE-BF72-4C25-B768-8295906E5CF8") || sequenceFlows.get(2).getId().equals("sid-D2491B73-0382-4EC2-AAAC-C8FD129E4CBE")
        || sequenceFlows.get(2).getId().equals("sid-7036D56C-E8EF-493B-ADEC-57EED4C6CE1F")).isTrue();
    assertThat(sequenceFlows.get(0).getSourceRef().equals("parallel1")).isTrue();
    assertThat(sequenceFlows.get(1).getSourceRef().equals("parallel1")).isTrue();
    assertThat(sequenceFlows.get(2).getSourceRef().equals("parallel1")).isTrue();
    flowElement = model.getMainProcess().getFlowElement("parallel2", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(ParallelGateway.class);
    gateway = (ParallelGateway) flowElement;
    sequenceFlows = gateway.getIncomingFlows();
    assertThat(sequenceFlows.size() == 3).isTrue();
    assertThat(sequenceFlows.get(0).getId().equals("sid-4C19E041-42FA-485D-9D09-D47CCD9DB270") || sequenceFlows.get(0).getId().equals("sid-05A991A6-0296-4867-ACBA-EF9EEC68FB8A")
        || sequenceFlows.get(0).getId().equals("sid-C546AC84-379D-4094-9DC3-548593F2EA0D")).isTrue();
    assertThat(sequenceFlows.get(1).getId().equals("sid-4C19E041-42FA-485D-9D09-D47CCD9DB270") || sequenceFlows.get(1).getId().equals("sid-05A991A6-0296-4867-ACBA-EF9EEC68FB8A")
        || sequenceFlows.get(1).getId().equals("sid-C546AC84-379D-4094-9DC3-548593F2EA0D")).isTrue();
    assertThat(sequenceFlows.get(2).getId().equals("sid-4C19E041-42FA-485D-9D09-D47CCD9DB270") || sequenceFlows.get(2).getId().equals("sid-05A991A6-0296-4867-ACBA-EF9EEC68FB8A")
        || sequenceFlows.get(2).getId().equals("sid-C546AC84-379D-4094-9DC3-548593F2EA0D")).isTrue();
    assertThat(sequenceFlows.get(0).getTargetRef().equals("parallel2")).isTrue();
    assertThat(sequenceFlows.get(1).getTargetRef().equals("parallel2")).isTrue();
    assertThat(sequenceFlows.get(2).getTargetRef().equals("parallel2")).isTrue();
  }

  protected String getResource() {
    return "test.flownodemultipleoutgoingflowsmodel.json";
  }

}
