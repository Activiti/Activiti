package org.activiti.editor.language.java;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

public class SimpleModelTest {

  @Test
  public void connvertModelToXML() throws Exception {
    BpmnModel bpmnModel = new BpmnModel();
    Process process = new Process();
    process.setId("simpleProcess");
    process.setName("Very simple process");
    bpmnModel.getProcesses().add(process);
    StartEvent startEvent = new StartEvent();
    startEvent.setId("startEvent1");
    process.addFlowElement(startEvent);
    UserTask task = new UserTask();
    task.setId("reviewTask");
    task.setAssignee("kermit");
    process.addFlowElement(task);
    SequenceFlow flow1 = new SequenceFlow();
    flow1.setId("flow1");
    flow1.setSourceRef("startEvent1");
    flow1.setTargetRef("reviewTask");
    process.addFlowElement(flow1);
    EndEvent endEvent = new EndEvent();
    endEvent.setId("endEvent1");
    process.addFlowElement(endEvent);
    
    BpmnXMLConverter converter = new BpmnXMLConverter();
    System.out.println(new String(converter.convertToXML(bpmnModel)));
  }
}
