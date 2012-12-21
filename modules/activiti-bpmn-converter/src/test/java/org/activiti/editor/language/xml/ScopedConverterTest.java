package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

public class ScopedConverterTest extends AbstractConverterTest {
  
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
    return "scopedmodel.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("outerSubProcess");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof SubProcess);
    assertEquals("outerSubProcess", flowElement.getId());
    SubProcess outerSubProcess = (SubProcess) flowElement;
    List<BoundaryEvent> eventList = outerSubProcess.getBoundaryEvents();
    assertEquals(1, eventList.size());
    BoundaryEvent boundaryEvent = eventList.get(0);
    assertEquals("outerBoundaryEvent", boundaryEvent.getId());
    
    FlowElement subElement = outerSubProcess.getFlowElement("innerSubProcess");
    assertNotNull(subElement);
    assertTrue(subElement instanceof SubProcess);
    assertEquals("innerSubProcess", subElement.getId());
    SubProcess innerSubProcess = (SubProcess) subElement;
    eventList = innerSubProcess.getBoundaryEvents();
    assertEquals(1, eventList.size());
    boundaryEvent = eventList.get(0);
    assertEquals("innerBoundaryEvent", boundaryEvent.getId());
    
    FlowElement taskElement = innerSubProcess.getFlowElement("usertask");
    assertNotNull(taskElement);
    assertTrue(taskElement instanceof UserTask);
    UserTask userTask = (UserTask) taskElement;
    assertEquals("usertask", userTask.getId());
    eventList = userTask.getBoundaryEvents();
    assertEquals(1, eventList.size());
    boundaryEvent = eventList.get(0);
    assertEquals("taskBoundaryEvent", boundaryEvent.getId());
  }
}
