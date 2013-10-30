package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
  public void connvertXMLToModel() throws Exception {
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
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof StartEvent);
    assertEquals("start1", flowElement.getId());
    
    flowElement = model.getMainProcess().getFlowElement("userTask1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof UserTask);
    assertEquals("userTask1", flowElement.getId());
    UserTask userTask = (UserTask) flowElement;
    assertTrue(userTask.getCandidateUsers().size() == 1);
    assertTrue(userTask.getCandidateGroups().size() == 1);
    assertTrue(userTask.getFormProperties().size() == 2);
    
    flowElement = model.getMainProcess().getFlowElement("subprocess1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof SubProcess);
    assertEquals("subprocess1", flowElement.getId());
    SubProcess subProcess = (SubProcess) flowElement;
    assertTrue(subProcess.getLoopCharacteristics().isSequential());
    assertEquals("10", subProcess.getLoopCharacteristics().getLoopCardinality());
    assertEquals("${assignee == \"\"}", subProcess.getLoopCharacteristics().getCompletionCondition());
    assertTrue(subProcess.getFlowElements().size() == 5);
    
    assertEquals(1, subProcess.getExecutionListeners().size());
    ActivitiListener listenerSubProcess = subProcess.getExecutionListeners().get(0);
    assertEquals("SubProcessTestClass", listenerSubProcess.getImplementation());
    assertEquals(ImplementationType.IMPLEMENTATION_TYPE_CLASS, listenerSubProcess.getImplementationType());
    assertEquals("start", listenerSubProcess.getEvent());    
    
    flowElement = model.getMainProcess().getFlowElement("boundaryEvent1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof BoundaryEvent);
    assertEquals("boundaryEvent1", flowElement.getId());
    BoundaryEvent boundaryEvent = (BoundaryEvent) flowElement;
    assertNotNull(boundaryEvent.getAttachedToRef());
    assertEquals("subprocess1", boundaryEvent.getAttachedToRef().getId());
    assertEquals(1, boundaryEvent.getEventDefinitions().size());
    assertTrue(boundaryEvent.getEventDefinitions().get(0) instanceof TimerEventDefinition);
    
    assertEquals(1, model.getMainProcess().getExecutionListeners().size());
    ActivitiListener listenerMainProcess = model.getMainProcess().getExecutionListeners().get(0);
    assertEquals("TestClass", listenerMainProcess.getImplementation());
    assertEquals(ImplementationType.IMPLEMENTATION_TYPE_CLASS, listenerMainProcess.getImplementationType());
    assertEquals("start", listenerMainProcess.getEvent());
  }
}
