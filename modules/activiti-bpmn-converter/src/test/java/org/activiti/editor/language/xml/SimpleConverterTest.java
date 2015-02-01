package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

public class SimpleConverterTest extends AbstractConverterTest {

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
    return "simplemodel.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    assertEquals(2, model.getDefinitionsAttributes().size());
    assertEquals("2.2A", model.getDefinitionsAttributeValue("http://activiti.com/modeler", "version"));
    assertEquals("20140312T10:45:23", model.getDefinitionsAttributeValue("http://activiti.com/modeler", "exportDate"));
    
    assertEquals("simpleProcess", model.getMainProcess().getId());
    assertEquals("Simple process", model.getMainProcess().getName());
    assertEquals("simple doc", model.getMainProcess().getDocumentation());
    assertEquals(true, model.getMainProcess().isExecutable());
    
    FlowElement flowElement = model.getMainProcess().getFlowElement("flow1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof SequenceFlow);
    assertEquals("flow1", flowElement.getId());
    
    flowElement = model.getMainProcess().getFlowElement("catchEvent");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof IntermediateCatchEvent);
    assertEquals("catchEvent", flowElement.getId());
    IntermediateCatchEvent catchEvent = (IntermediateCatchEvent) flowElement;
    assertTrue(catchEvent.getEventDefinitions().size() == 1);
    EventDefinition eventDefinition = catchEvent.getEventDefinitions().get(0);
    assertTrue(eventDefinition instanceof TimerEventDefinition);
    TimerEventDefinition timerDefinition = (TimerEventDefinition) eventDefinition;
    assertEquals("PT5M", timerDefinition.getTimeDuration());
    
    flowElement = model.getMainProcess().getFlowElement("userTask1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof UserTask);
    UserTask task = (UserTask) flowElement;
    assertEquals("task doc", task.getDocumentation());
    
    flowElement = model.getMainProcess().getFlowElement("flow1Condition");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof SequenceFlow);
    assertEquals("flow1Condition", flowElement.getId());
    SequenceFlow flow = (SequenceFlow) flowElement;
    assertEquals("${number <= 1}", flow.getConditionExpression());
    
    flowElement = model.getMainProcess().getFlowElement("gateway1");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof ExclusiveGateway);
  }
}
