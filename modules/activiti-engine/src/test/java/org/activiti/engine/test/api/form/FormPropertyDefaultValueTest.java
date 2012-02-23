package org.activiti.engine.test.api.form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

public class FormPropertyDefaultValueTest extends PluggableActivitiTestCase {

  @Deployment
  public void testDefaultValue() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("FormPropertyDefaultValueTest.testDefaultValue");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    TaskFormData formData = formService.getTaskFormData(task.getId());
    List<FormProperty> formProperties = formData.getFormProperties();
    assertEquals(4, formProperties.size());

    for (FormProperty prop : formProperties) {
      if ("booleanProperty".equals(prop.getId())) {
        assertEquals("true", prop.getValue());
      } else if ("stringProperty".equals(prop.getId())) {
        assertEquals("someString", prop.getValue());
      } else if ("longProperty".equals(prop.getId())) {
        assertEquals("42", prop.getValue());
      } else if ("longExpressionProperty".equals(prop.getId())) {
        assertEquals("23", prop.getValue());
      } else {
        assertTrue("Invalid form property: " + prop.getId(), false);
      }
    }

    Map<String, String> formDataUpdate = new HashMap<String, String>();
    formDataUpdate.put("longExpressionProperty", "1");
    formDataUpdate.put("booleanProperty", "false");
    formService.submitTaskFormData(task.getId(), formDataUpdate);

    assertEquals(false, runtimeService.getVariable(processInstance.getId(), "booleanProperty"));
    assertEquals("someString", runtimeService.getVariable(processInstance.getId(), "stringProperty"));
    assertEquals(42L, runtimeService.getVariable(processInstance.getId(), "longProperty"));
    assertEquals(1L, runtimeService.getVariable(processInstance.getId(), "longExpressionProperty"));
  }
}
