package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.junit.jupiter.api.Test;

public class FlowNodeConverterTest extends AbstractConverterTest {

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
    bpmnModel = convertToJsonAndBack(bpmnModel);
    // System.out.println("xml " + new String(new
    // BpmnXMLConverter().convertToXML(bpmnModel), "utf-8"));
    validateModel(bpmnModel);
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("sid-B074A0DD-934A-4053-A537-20ADF0781023", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(ExclusiveGateway.class);
    ExclusiveGateway gateway = (ExclusiveGateway) flowElement;
    List<SequenceFlow> sequenceFlows = gateway.getOutgoingFlows();
    assertThat(sequenceFlows.size() == 2).isTrue();
    assertThat(sequenceFlows.get(0).getId().equals("sid-07A7E174-8857-4DE9-A7CD-A041706D79C3") || sequenceFlows.get(0).getId().equals("sid-C2068B1E-9A82-41C9-B876-C58E2736C186")).isTrue();
    assertThat(sequenceFlows.get(1).getId().equals("sid-07A7E174-8857-4DE9-A7CD-A041706D79C3") || sequenceFlows.get(1).getId().equals("sid-C2068B1E-9A82-41C9-B876-C58E2736C186")).isTrue();
    assertThat(sequenceFlows.get(0).getSourceRef().equals("sid-B074A0DD-934A-4053-A537-20ADF0781023")).isTrue();
    assertThat(sequenceFlows.get(1).getSourceRef().equals("sid-B074A0DD-934A-4053-A537-20ADF0781023")).isTrue();
    assertThat(gateway.getDefaultFlow()).isEqualTo("sid-07A7E174-8857-4DE9-A7CD-A041706D79C3");
  }

  protected String getResource() {
    return "test.flownodemodel.json";
  }

}
