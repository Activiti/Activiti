package org.activiti.editor.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

public class FormPropertiesConverterTest extends AbstractConverterTest {

  @Test
  public void connvertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }
  
  @Test 
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }
  
  protected String getResource() {
    return "test.formpropertiesmodel.json";
  }
  
  private void validateModel(BpmnModel model) {
    assertEquals("formPropertiesProcess", model.getMainProcess().getId());
    assertEquals("User registration", model.getMainProcess().getName());
    assertEquals(true, model.getMainProcess().isExecutable());
    
    FlowElement startFlowElement = model.getMainProcess().getFlowElement("register");
    assertNotNull(startFlowElement);
    assertTrue(startFlowElement instanceof StartEvent);
    StartEvent startEvent = (StartEvent) startFlowElement;
    for (FormProperty formProperty :startEvent.getFormProperties()) {
    	assertEquals(true, formProperty.isRequired());
    }
    
    FlowElement userFlowElement = model.getMainProcess().getFlowElement("edit");
    assertNotNull(userFlowElement);
    assertTrue(userFlowElement instanceof UserTask);
    UserTask userTask = (UserTask) userFlowElement;
    for (FormProperty formProperty :userTask.getFormProperties()) {
    	if(formProperty.getId().equals("duplicated")){
    		assertEquals(false, formProperty.isRequired());
    		assertEquals(true, formProperty.isReadable());
    		assertEquals(true, formProperty.isWriteable());
    	}
    	if(formProperty.getId().equals("name")){
    		assertEquals(false, formProperty.isRequired());
    		assertEquals(true, formProperty.isReadable());
    		assertEquals(true, formProperty.isWriteable());
    	}
    	if(formProperty.getId().equals("age")){
    		assertEquals(false, formProperty.isRequired());
    		assertEquals(true, formProperty.isReadable());
    		assertEquals(false, formProperty.isWriteable());
    	}
    	if(formProperty.getId().equals("isMan")){
    		assertEquals(false, formProperty.isRequired());
    		assertEquals(false, formProperty.isReadable());
    		assertEquals(true, formProperty.isWriteable());
    	}
    	if(formProperty.getId().equals("remove")){
    		assertEquals(true, formProperty.isRequired());
    		assertEquals(true, formProperty.isReadable());
    		assertEquals(true, formProperty.isWriteable());
    	}
    }
  }
}
