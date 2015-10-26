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

    FlowElement startFlowElement = model.getMainProcess().getFlowElement("startNode");
    assertNotNull(startFlowElement);
    assertTrue(startFlowElement instanceof StartEvent);
    StartEvent startEvent = (StartEvent) startFlowElement;

    for (FormProperty formProperty :startEvent.getFormProperties()) {
      assertEquals(true, formProperty.isRequired());
    }

    FlowElement userFlowElement = model.getMainProcess().getFlowElement("userTask");
    assertNotNull(userFlowElement);
    assertTrue(userFlowElement instanceof UserTask);
    UserTask userTask = (UserTask) userFlowElement;

    List<FormProperty> formProperties = userTask.getFormProperties();

    assertNotNull(formProperties);
    assertEquals("Invalid form properties list: ", 8, formProperties.size());

    for (FormProperty formProperty :formProperties) {
      if (formProperty.getId().equals("new_property_1")) {
        checkFormProperty(formProperty, false, false, false);
      } else if (formProperty.getId().equals("new_property_2")) {
        checkFormProperty(formProperty, false, false, true);
      } else if (formProperty.getId().equals("new_property_3")) {
        checkFormProperty(formProperty, false, true, false);
      } else if (formProperty.getId().equals("new_property_4")) {
        checkFormProperty(formProperty, false, true, true);
      } else if (formProperty.getId().equals("new_property_5")) {
        checkFormProperty(formProperty, true, false, false);

        List<Map<String, Object>> formValues = new ArrayList<Map<String,Object>>();
        for (FormValue formValue : formProperty.getFormValues()) {
          Map<String, Object> formValueMap = new HashMap<String, Object>();
          formValueMap.put("id", formValue.getId());
          formValueMap.put("name", formValue.getName());
          formValues.add(formValueMap);
        }
        checkFormPropertyFormValues(formValues);

      } else if (formProperty.getId().equals("new_property_6")) {
        checkFormProperty(formProperty, true, false, true);
      } else if (formProperty.getId().equals("new_property_7")) {
        checkFormProperty(formProperty, true, true, false);
      } else if (formProperty.getId().equals("new_property_8")) {
        checkFormProperty(formProperty, true, true, true);
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
    formValue1.put("id", "value1");
    formValue1.put("name", "Value 1");
    Map<String, Object> formValue2 = new HashMap<String, Object>();
    formValue2.put("id", "value2");
    formValue2.put("name", "Value 2");

    Map<String, Object> formValue3 = new HashMap<String, Object>();
    formValue3.put("id", "value3");
    formValue3.put("name", "Value 3");

    Map<String, Object> formValue4 = new HashMap<String, Object>();
    formValue4.put("id", "value4");
    formValue4.put("name", "Value 4");

    expectedFormValues.add(formValue1);
    expectedFormValues.add(formValue2);
    expectedFormValues.add(formValue3);
    expectedFormValues.add(formValue4);

    assertEquals(expectedFormValues, formValues);
  }
}
