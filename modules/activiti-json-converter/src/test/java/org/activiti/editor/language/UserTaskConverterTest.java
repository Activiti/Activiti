package org.activiti.editor.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class UserTaskConverterTest extends AbstractConverterTest {

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
  
  protected String getResource() {
    return "test.usertaskmodel.json";
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("usertask");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof UserTask);
    assertEquals("usertask", flowElement.getId());
    UserTask userTask = (UserTask) flowElement;
    assertEquals("usertask", userTask.getId());
    assertEquals("User task", userTask.getName());
    assertEquals("testKey", userTask.getFormKey());
    assertEquals("40", userTask.getPriority());
    assertEquals("2012-11-01", userTask.getDueDate());
    assertEquals("defaultCategory", userTask.getCategory());
    
    assertEquals("gonzo", userTask.getOwner());
    assertEquals("kermit", userTask.getAssignee());
    assertEquals(2, userTask.getCandidateUsers().size());
    assertTrue(userTask.getCandidateUsers().contains("kermit"));
    assertTrue(userTask.getCandidateUsers().contains("fozzie"));
    assertEquals(2, userTask.getCandidateGroups().size());
    assertTrue(userTask.getCandidateGroups().contains("management"));
    assertTrue(userTask.getCandidateGroups().contains("sales"));
    
    List<FormProperty> formProperties = userTask.getFormProperties();
    assertEquals(2, formProperties.size());
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
    
    List<ActivitiListener> listeners = userTask.getTaskListeners();
    assertEquals(3, listeners.size());
    ActivitiListener listener = (ActivitiListener) listeners.get(0);
    assertTrue(ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType()));
    assertEquals("org.test.TestClass", listener.getImplementation());
    assertEquals("create", listener.getEvent());
    assertEquals(2, listener.getFieldExtensions().size());
    assertEquals("testField", listener.getFieldExtensions().get(0).getFieldName());
    assertEquals("test", listener.getFieldExtensions().get(0).getStringValue());
    listener = (ActivitiListener) listeners.get(1);
    assertTrue(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(listener.getImplementationType()));
    assertEquals("${someExpression}", listener.getImplementation());
    assertEquals("assignment", listener.getEvent());
    listener = (ActivitiListener) listeners.get(2);
    assertTrue(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(listener.getImplementationType()));
    assertEquals("${someDelegateExpression}", listener.getImplementation());
    assertEquals("complete", listener.getEvent());
    
    flowElement = model.getMainProcess().getFlowElement("start");
    assertTrue(flowElement instanceof StartEvent);
    
    StartEvent startEvent  = (StartEvent) flowElement;
    assertTrue(startEvent.getOutgoingFlows().size() == 1);
    
    flowElement = model.getMainProcess().getFlowElement("flow1");
    assertTrue(flowElement instanceof SequenceFlow);
    
    SequenceFlow flow  = (SequenceFlow) flowElement;
    assertEquals("flow1", flow.getId());
    assertNotNull(flow.getSourceRef());
    assertNotNull(flow.getTargetRef());
  }
}
