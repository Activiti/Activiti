package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

public class FormPropertiesConverterTest extends AbstractConverterTest {

  @Test
  public void connvertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }
  
  @Test 
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
    bpmnModel = exportAndReadXMLFile(bpmnModel);
    validateModel(bpmnModel);
  }
  
  protected String getResource() {
    return "formPropertiesProcess.bpmn";
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
      if (formProperty.getId().equals("duplicated")) {
        checkFormProperty(formProperty, false, true, true);
      }
      if (formProperty.getId().equals("name")) {
        checkFormProperty(formProperty, false, true, true);
      }
      if (formProperty.getId().equals("age")) {
        checkFormProperty(formProperty, false, true, false);
      }
      if (formProperty.getId().equals("isMan")) {
        checkFormProperty(formProperty, false, false, true);
      }
      if (formProperty.getId().equals("remove")) {
        checkFormProperty(formProperty, true, true, true);
      }
      if (formProperty.getId().equals("testEnum")) {
      checkFormProperty(formProperty, false, true, true);
      
      List<Map<String, Object>> formValues = new ArrayList<Map<String,Object>>();
      for (FormValue formValue : formProperty.getFormValues()) {
        Map<String, Object> formValueMap = new HashMap<String, Object>();
        formValueMap.put("id", formValue.getId());
        formValueMap.put("name", formValue.getName());
        formValues.add(formValueMap);
      }
      checkFormPropertyFormValues(formValues);
    }
    }
  }
  
  private void checkFormProperty(FormProperty formProperty, boolean shouldBeRequired, boolean shouldBeReadable, boolean shouldBeWritable) {
    assertEquals(shouldBeRequired, formProperty.isRequired());
    assertEquals(shouldBeReadable, formProperty.isReadable());
    assertEquals(shouldBeWritable, formProperty.isWriteable());
  }
  private void checkFormPropertyFormValues(List<Map<String, Object>> formValues) {
    List<Map<String, Object>> expectedFormValues = new ArrayList<Map<String,Object>>();
    Map<String, Object> formValue1 = new HashMap<String, Object>();
    formValue1.put("id", "enum1");
    formValue1.put("name", "enum1");
    Map<String, Object> formValue2 = new HashMap<String, Object>();
    formValue2.put("id", "enum2");
    formValue2.put("name", "enum2");
    expectedFormValues.add(formValue1);
    expectedFormValues.add(formValue2);
    assertEquals(expectedFormValues, formValues);
  }
}
