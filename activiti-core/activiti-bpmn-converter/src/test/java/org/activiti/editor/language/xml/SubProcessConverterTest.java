package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

public class SubProcessConverterTest extends AbstractConverterTest {

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
    return "subprocessmodel.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("start1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement instanceof StartEvent).isTrue();
    assertThat(flowElement.getId()).isEqualTo("start1");

    flowElement = model.getMainProcess().getFlowElement("userTask1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement instanceof UserTask).isTrue();
    assertThat(flowElement.getId()).isEqualTo("userTask1");
    UserTask userTask = (UserTask) flowElement;
    assertThat(userTask.getCandidateUsers().size() == 1).isTrue();
    assertThat(userTask.getCandidateGroups().size() == 1).isTrue();
    assertThat(userTask.getFormProperties().size() == 2).isTrue();

    flowElement = model.getMainProcess().getFlowElement("subprocess1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement instanceof SubProcess).isTrue();
    assertThat(flowElement.getId()).isEqualTo("subprocess1");
    SubProcess subProcess = (SubProcess) flowElement;
    assertThat(subProcess.getLoopCharacteristics().isSequential()).isTrue();
    assertThat(subProcess.getLoopCharacteristics().getLoopCardinality()).isEqualTo("10");
    assertThat(subProcess.getLoopCharacteristics().getCompletionCondition()).isEqualTo("${assignee == \"\"}");
    assertThat(subProcess.getFlowElements().size() == 5).isTrue();

    assertThat(subProcess.getExecutionListeners().size()).isEqualTo(1);
    ActivitiListener listenerSubProcess = subProcess.getExecutionListeners().get(0);
    assertThat(listenerSubProcess.getImplementation()).isEqualTo("SubProcessTestClass");
    assertThat(listenerSubProcess.getImplementationType()).isEqualTo(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
    assertThat(listenerSubProcess.getEvent()).isEqualTo("start");

    flowElement = model.getMainProcess().getFlowElement("boundaryEvent1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement instanceof BoundaryEvent).isTrue();
    assertThat(flowElement.getId()).isEqualTo("boundaryEvent1");
    BoundaryEvent boundaryEvent = (BoundaryEvent) flowElement;
    assertThat(boundaryEvent.getAttachedToRef()).isNotNull();
    assertThat(boundaryEvent.getAttachedToRef().getId()).isEqualTo("subprocess1");
    assertThat(boundaryEvent.getEventDefinitions().size()).isEqualTo(1);
    assertThat(boundaryEvent.getEventDefinitions().get(0) instanceof TimerEventDefinition).isTrue();

    assertThat(model.getMainProcess().getExecutionListeners().size()).isEqualTo(1);
    ActivitiListener listenerMainProcess = model.getMainProcess().getExecutionListeners().get(0);
    assertThat(listenerMainProcess.getImplementation()).isEqualTo("TestClass");
    assertThat(listenerMainProcess.getImplementationType()).isEqualTo(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
    assertThat(listenerMainProcess.getEvent()).isEqualTo("start");
  }
}
