package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.junit.jupiter.api.Test;

public class PoolsConverterTest extends AbstractConverterTest {

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
    return "pools.bpmn";
  }

  private void validateModel(BpmnModel model) {
    assertThat(model.getPools()).hasSize(1);
    Pool pool = model.getPools().get(0);
    assertThat(pool.getId()).isEqualTo("pool1");
    assertThat(pool.getName()).isEqualTo("Pool");
    Process process = model.getProcess(pool.getId());
    assertThat(process).isNotNull();
    assertThat(process.getLanes()).hasSize(2);
    Lane lane = process.getLanes().get(0);
    assertThat(lane.getId()).isEqualTo("lane1");
    assertThat(lane.getName()).isEqualTo("Lane 1");
    assertThat(lane.getFlowReferences()).hasSize(2);
    lane = process.getLanes().get(1);
    assertThat(lane.getId()).isEqualTo("lane2");
    assertThat(lane.getName()).isEqualTo("Lane 2");
    assertThat(lane.getFlowReferences()).hasSize(2);
    FlowElement flowElement = process.getFlowElement("flow1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(SequenceFlow.class);
  }
}
