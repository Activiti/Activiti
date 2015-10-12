package org.activiti.engine.impl.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BpmnCacheUtil {
  
  public static final String BPMN_NODE = "bpmn";
  
  public static final String SERVICE_TASK_CLASS_NAME = "serviceTaskClassName";
  public static final String SERVICE_TASK_EXPRESSION = "serviceTaskExpression";
  public static final String SERVICE_TASK_DELEGATE_EXPRESSION = "serviceTaskDelegateExpression";
  
  public static final String USER_TASK_FORM_KEY = "userTaskFormKey";
  
  protected static ObjectMapper objectMapper = new ObjectMapper();

  public static void changeClassName(String id, String className, ObjectNode infoNode) {
    setElementProperty(id, SERVICE_TASK_CLASS_NAME, className, infoNode);
  }
  
  public static void changeExpression(String id, String expression, ObjectNode infoNode) {
    setElementProperty(id, SERVICE_TASK_EXPRESSION, expression, infoNode);
  }
  
  public static void changeDelegateExpression(String id, String expression, ObjectNode infoNode) {
    setElementProperty(id, SERVICE_TASK_DELEGATE_EXPRESSION, expression, infoNode);
  }
  
  public static void changeFormKey(String id, String formKey, ObjectNode infoNode) {
    setElementProperty(id, USER_TASK_FORM_KEY, formKey, infoNode);
  }
  
  public static ObjectNode getElementProperties(String id, ObjectNode infoNode) {
    ObjectNode propertiesNode = null;
    ObjectNode bpmnNode = getBpmnNode(infoNode);
    if (bpmnNode != null) {
      propertiesNode = (ObjectNode) bpmnNode.get(id);
    }
    return propertiesNode;
  }
  
  protected static void setElementProperty(String id, String propertyName, String propertyValue, ObjectNode infoNode) {
    ObjectNode bpmnNode = createOrGetBpmnNode(infoNode);
    if (bpmnNode.has(id) == false) {
      bpmnNode.put(id, objectMapper.createObjectNode());
    }
    
    ((ObjectNode) bpmnNode.get(id)).put(propertyName, propertyValue);
  }
  
  protected static ObjectNode createOrGetBpmnNode(ObjectNode infoNode) {
    if (infoNode.has(BPMN_NODE) == false) {
      infoNode.put(BPMN_NODE, objectMapper.createObjectNode());
    }
    return (ObjectNode) infoNode.get(BPMN_NODE);
  }
  
  protected static ObjectNode getBpmnNode(ObjectNode infoNode) {
    return (ObjectNode) infoNode.get(BPMN_NODE);
  }
}
