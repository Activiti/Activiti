package org.activiti.editor.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.StartEvent;
import org.junit.Test;


public class StartEventConverterTest extends AbstractConverterTest {

  @Test
  public void connvertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }
  
  @Test 
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }
  
  @Override
  protected String getResource() {
    return "test.starteventmodel.json";
  }

  private void validateModel(BpmnModel model) {
    
    FlowElement flowElement = model.getMainProcess().getFlowElement("start");
    assertTrue(flowElement instanceof StartEvent);
    
    StartEvent startEvent  = (StartEvent) flowElement;
    assertEquals("start", startEvent.getId());
    assertEquals("startName", startEvent.getName());
    assertEquals("startFormKey", startEvent.getFormKey());
    assertEquals("startInitiator", startEvent.getInitiator());
    assertEquals("startDoc", startEvent.getDocumentation());
    
    List<FormProperty> formProperties = startEvent.getFormProperties();
    assertEquals(2, formProperties.size());
    
    FormProperty formProperty = formProperties.get(0);
    assertEquals("startFormProp1", formProperty.getId());
    assertEquals("startFormProp1", formProperty.getName());
    assertEquals("string", formProperty.getType());
    
    formProperty = formProperties.get(1);
    assertEquals("startFormProp2", formProperty.getId());
    assertEquals("startFormProp2", formProperty.getName());
    assertEquals("boolean", formProperty.getType());

  }
  
}
