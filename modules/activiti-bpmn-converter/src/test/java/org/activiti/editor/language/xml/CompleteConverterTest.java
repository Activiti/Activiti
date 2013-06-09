package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    return "completemodel.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("userTask1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof UserTask);
    assertEquals("userTask1", flowElement.getId());
    
    flowElement = model.getMainProcess().getFlowElement("catchsignal");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof IntermediateCatchEvent);
    assertEquals("catchsignal", flowElement.getId());
    IntermediateCatchEvent catchEvent = (IntermediateCatchEvent) flowElement;
    assertEquals(1, catchEvent.getEventDefinitions().size());
    assertTrue(catchEvent.getEventDefinitions().get(0) instanceof SignalEventDefinition);
    SignalEventDefinition signalEvent = (SignalEventDefinition) catchEvent.getEventDefinitions().get(0);
    assertEquals("testSignal", signalEvent.getSignalRef());
    
    flowElement = model.getMainProcess().getFlowElement("subprocess");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof SubProcess);
    assertEquals("subprocess", flowElement.getId());
    SubProcess subProcess = (SubProcess) flowElement;
    
    flowElement = subProcess.getFlowElement("receiveTask");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof ReceiveTask);
    assertEquals("receiveTask", flowElement.getId());
  }
}
