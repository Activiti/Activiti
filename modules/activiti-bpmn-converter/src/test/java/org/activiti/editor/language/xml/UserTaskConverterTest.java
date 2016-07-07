package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.UserTask;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class UserTaskConverterTest extends AbstractConverterTest {

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
    return "usertaskmodel.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("usertask");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof UserTask);
    assertEquals("usertask", flowElement.getId());
    UserTask userTask = (UserTask) flowElement;
    assertEquals("usertask", userTask.getId());
    assertEquals("User task", userTask.getName());
    assertEquals("Test Category", userTask.getCategory());
    assertEquals("testKey", userTask.getFormKey());
    assertEquals("40", userTask.getPriority());
    assertEquals("2012-11-01", userTask.getDueDate());
    assertEquals("customCalendarName", userTask.getBusinessCalendarName());

    assertEquals("kermit", userTask.getAssignee());
    assertEquals(2, userTask.getCandidateUsers().size());
    assertTrue(userTask.getCandidateUsers().contains("kermit"));
    assertTrue(userTask.getCandidateUsers().contains("fozzie"));
    assertEquals(2, userTask.getCandidateGroups().size());
    assertTrue(userTask.getCandidateGroups().contains("management"));
    assertTrue(userTask.getCandidateGroups().contains("sales"));
    
    assertEquals(1, userTask.getCustomUserIdentityLinks().size());
    assertEquals(2, userTask.getCustomGroupIdentityLinks().size());
    assertTrue(userTask.getCustomUserIdentityLinks().get("businessAdministrator").contains("kermit"));
    assertTrue(userTask.getCustomGroupIdentityLinks().get("manager").contains("management"));
    assertTrue(userTask.getCustomGroupIdentityLinks().get("businessAdministrator").contains("management"));
    
    
    List<FormProperty> formProperties = userTask.getFormProperties();
    assertEquals(3, formProperties.size());
    FormProperty formProperty = formProperties.get(0);
    assertEquals("formId", formProperty.getId());
    assertEquals("formName", formProperty.getName());
    assertEquals("string", formProperty.getType());
    assertEquals("variable", formProperty.getVariable());
    assertEquals("${expression}", formProperty.getExpression());
    formProperty = formProperties.get(1);
    assertEquals("formId2", formProperty.getId());
    assertEquals("anotherName", formProperty.getName());
    assertEquals("long", formProperty.getType());
    assertTrue(StringUtils.isEmpty(formProperty.getVariable()));
    assertTrue(StringUtils.isEmpty(formProperty.getExpression()));
    formProperty = formProperties.get(2);
    assertEquals("formId3", formProperty.getId());
    assertEquals("enumName", formProperty.getName());
    assertEquals("enum", formProperty.getType());
    assertTrue(StringUtils.isEmpty(formProperty.getVariable()));
    assertTrue(StringUtils.isEmpty(formProperty.getExpression()));
    assertEquals(2, formProperty.getFormValues().size());
    
    List<ActivitiListener> listeners = userTask.getTaskListeners();
    assertEquals(3, listeners.size());
    ActivitiListener listener = listeners.get(0);
    assertTrue(ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType()));
    assertEquals("org.test.TestClass", listener.getImplementation());
    assertEquals("create", listener.getEvent());
    listener = listeners.get(1);
    assertTrue(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(listener.getImplementationType()));
    assertEquals("${someExpression}", listener.getImplementation());
    assertEquals("assignment", listener.getEvent());
    listener = listeners.get(2);
    assertTrue(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(listener.getImplementationType()));
    assertEquals("${someDelegateExpression}", listener.getImplementation());
    assertEquals("complete", listener.getEvent());
    
    List<ActivitiListener> executionListeners = userTask.getExecutionListeners();
    assertEquals(1, executionListeners.size());
    ActivitiListener executionListener = executionListeners.get(0);
    assertEquals("end", executionListener.getEvent());
    
  }
}
