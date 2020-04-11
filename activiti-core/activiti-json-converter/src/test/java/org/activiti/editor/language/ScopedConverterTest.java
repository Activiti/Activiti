package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

public class ScopedConverterTest extends AbstractConverterTest {

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
    return "test.scopedmodel.json";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("outerSubProcess", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement instanceof SubProcess).isTrue();
    assertThat(flowElement.getId()).isEqualTo("outerSubProcess");
    SubProcess outerSubProcess = (SubProcess) flowElement;
    List<BoundaryEvent> eventList = outerSubProcess.getBoundaryEvents();
    assertThat(eventList.size()).isEqualTo(1);
    BoundaryEvent boundaryEvent = eventList.get(0);
    assertThat(boundaryEvent.getId()).isEqualTo("outerBoundaryEvent");

    FlowElement subElement = outerSubProcess.getFlowElement("innerSubProcess");
    assertThat(subElement).isNotNull();
    assertThat(subElement instanceof SubProcess).isTrue();
    assertThat(subElement.getId()).isEqualTo("innerSubProcess");
    SubProcess innerSubProcess = (SubProcess) subElement;
    eventList = innerSubProcess.getBoundaryEvents();
    assertThat(eventList.size()).isEqualTo(1);
    boundaryEvent = eventList.get(0);
    assertThat(boundaryEvent.getId()).isEqualTo("innerBoundaryEvent");

    FlowElement taskElement = innerSubProcess.getFlowElement("usertask");
    assertThat(taskElement).isNotNull();
    assertThat(taskElement instanceof UserTask).isTrue();
    UserTask userTask = (UserTask) taskElement;
    assertThat(userTask.getId()).isEqualTo("usertask");
    eventList = userTask.getBoundaryEvents();
    assertThat(eventList.size()).isEqualTo(1);
    boundaryEvent = eventList.get(0);
    assertThat(boundaryEvent.getId()).isEqualTo("taskBoundaryEvent");
  }
}
