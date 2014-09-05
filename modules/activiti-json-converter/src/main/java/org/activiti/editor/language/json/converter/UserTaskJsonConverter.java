/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.editor.language.json.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.util.CommaSplitter;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class UserTaskJsonConverter extends BaseBpmnJsonConverter {
  private static final String USER = "user";
  private static final String GROUP = "group";
  private static final String COMMA_SPACE = ", ";
  private static final String COLON = ":";
  private static final String LEFT_PAREN = "(";
  private static final String RIGHT_PAREN = ")";

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    
    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }
  
  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_TASK_USER, UserTaskJsonConverter.class);
  }
  
  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(UserTask.class, UserTaskJsonConverter.class);
  }
  
  @Override
  protected String getStencilId(FlowElement flowElement) {
    return STENCIL_TASK_USER;
  }
  
  @Override
  protected void convertElementToJson(ObjectNode propertiesNode, FlowElement flowElement) {
    UserTask userTask = (UserTask) flowElement;
    String assignee = userTask.getAssignee();
    String candidateUsers = convertListToCommaSeparatedString(userTask.getCandidateUsers());
    String candidateGroups = convertListToCommaSeparatedString(userTask.getCandidateGroups());
    
    Map<String, Map<String, String>> resources = getCustomResources((UserTask)flowElement);

    if (StringUtils.isNotEmpty(assignee) || StringUtils.isNotEmpty(candidateUsers) || StringUtils.isNotEmpty(candidateGroups) || !resources.isEmpty()) {
      ObjectNode assignmentNode = objectMapper.createObjectNode();
      ArrayNode itemsNode = objectMapper.createArrayNode();
      
      if (assignee.length() > 0) {
        ObjectNode assignmentItemNode = objectMapper.createObjectNode();
        assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_TYPE, PROPERTY_USERTASK_ASSIGNEE);
        assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, assignee);
        itemsNode.add(assignmentItemNode);
      }
      
      if (candidateUsers.length() > 0) {
        ObjectNode assignmentItemNode = objectMapper.createObjectNode();
        assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_TYPE, PROPERTY_USERTASK_CANDIDATE_USERS);
        assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, candidateUsers);
        itemsNode.add(assignmentItemNode);
      }
      
      if (candidateGroups.length() > 0) {
        ObjectNode assignmentItemNode = objectMapper.createObjectNode();
        assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_TYPE, PROPERTY_USERTASK_CANDIDATE_GROUPS);
        assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, candidateGroups);
        itemsNode.add(assignmentItemNode);
      }
    
      if (!resources.isEmpty()) {
        for (String resourceType : resources.keySet())
        {
          Map<String, String> resourceMap = resources.get(resourceType);
          for (String identityType: resourceMap.keySet())
          {
            ObjectNode assignmentItemNode = objectMapper.createObjectNode();
            assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_TYPE, resourceType);
            StringBuffer assignmentTypeValue = new StringBuffer(21 + resourceType.length());
            if (USER.equals(identityType))
            {
              assignmentTypeValue.append(PROPERTY_USERTASK_CUSTOM_RESOURCE_USERS);
            }
            else
            {
              assignmentTypeValue.append(PROPERTY_USERTASK_CUSTOM_RESOURCE_GROUPS);
            }
            assignmentTypeValue.append(COLON);
            assignmentTypeValue.append(resourceType);
            assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_TYPE, assignmentTypeValue.toString());
            assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, resourceMap.get(identityType));
            itemsNode.add(assignmentItemNode);
          }
        }
      }
      
      assignmentNode.put("totalCount", itemsNode.size());
      assignmentNode.put(EDITOR_PROPERTIES_GENERAL_ITEMS, itemsNode);
      propertiesNode.put(PROPERTY_USERTASK_ASSIGNMENT, assignmentNode);
    }
    
    if (userTask.getPriority() != null) {
      setPropertyValue(PROPERTY_PRIORITY, userTask.getPriority().toString(), propertiesNode);
    }
    setPropertyValue(PROPERTY_FORMKEY, userTask.getFormKey(), propertiesNode);
    setPropertyValue(PROPERTY_DUEDATE, userTask.getDueDate(), propertiesNode);
    setPropertyValue(PROPERTY_CATEGORY, userTask.getCategory(), propertiesNode);;
    
    addFormProperties(userTask.getFormProperties(), propertiesNode);
  }
  
  @Override
  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    UserTask task = new UserTask();
    task.setPriority(getPropertyValueAsString(PROPERTY_PRIORITY, elementNode));
    task.setFormKey(getPropertyValueAsString(PROPERTY_FORMKEY, elementNode));
    task.setDueDate(getPropertyValueAsString(PROPERTY_DUEDATE, elementNode));
    task.setCategory(getPropertyValueAsString(PROPERTY_CATEGORY, elementNode));
    
    JsonNode assignmentNode = getProperty(PROPERTY_USERTASK_ASSIGNMENT, elementNode);
    if (assignmentNode != null) {
      JsonNode itemsNode = assignmentNode.get(EDITOR_PROPERTIES_GENERAL_ITEMS);
      if (itemsNode != null) {
        Iterator<JsonNode> assignmentIterator = itemsNode.elements();
        while (assignmentIterator.hasNext()) {
          JsonNode assignmentItemNode = assignmentIterator.next();
          if (assignmentItemNode.get(PROPERTY_USERTASK_ASSIGNMENT_TYPE) != null && 
              assignmentItemNode.get(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION) != null) {
            
            String assignmentType = assignmentItemNode.get(PROPERTY_USERTASK_ASSIGNMENT_TYPE).asText();
            if (PROPERTY_USERTASK_ASSIGNEE.equals(assignmentType)) {
              task.setAssignee(assignmentItemNode.get(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION).asText());
            } else if (PROPERTY_USERTASK_CANDIDATE_USERS.equals(assignmentType)) {
              task.setCandidateUsers(getValueAsList(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, assignmentItemNode));
            } else if (PROPERTY_USERTASK_CANDIDATE_GROUPS.equals(assignmentType)) {
              task.setCandidateGroups(getValueAsList(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, assignmentItemNode));
            } else if (PROPERTY_USERTASK_CUSTOM_RESOURCE_USERS.equals(assignmentType.split(COLON)[0])) {
              setCustomResource(USER, assignmentType.split(COLON)[1], getValueAsString(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, assignmentItemNode), task);
            } else if (PROPERTY_USERTASK_CUSTOM_RESOURCE_GROUPS.equals(assignmentType.split(COLON)[0])) {
              setCustomResource(GROUP, assignmentType.split(COLON)[1], getValueAsString(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, assignmentItemNode), task);
            }
          }
        }
      }
    }
    convertJsonToFormProperties(elementNode, task);
    return task;
  }
  
  public void setCustomResource(String identityType, String resourceType, String resourceExpression, UserTask userTask) {
    Map<String, List<ExtensionElement>> extensions = userTask.getExtensionElements();

    // if it already exists, add it to the existing one
    List<ExtensionElement> resourceElements = extensions.get(BpmnXMLConstants.ELEMENT_CUSTOM_RESOURCE);

    if (null != resourceElements && !resourceElements.isEmpty())
    {
      for (ExtensionElement resourceElement : resourceElements)
      {
        String attributeValue = resourceElement.getAttributeValue(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE, BpmnXMLConstants.ATTRIBUTE_NAME);
        if (null != attributeValue && resourceType.equals(attributeValue))
        {
          updateResourceExpression(identityType, resourceExpression, resourceElement);
        }
      }
    }
    else
    {
      userTask.addExtensionElement(generateResourceElement(identityType, resourceType, resourceExpression));
    }
  }

  protected ExtensionElement generateResourceElement(String identityType, String resourceType, String resourceExpression)
  {
    ExtensionElement resourceElement = new ExtensionElement();
    ExtensionElement resourceAssignmentElement = new ExtensionElement();
    ExtensionElement formalExpressionElement = new ExtensionElement();
    ExtensionAttribute resourceAttribute = new ExtensionAttribute();
    StringBuffer finalExpression = new StringBuffer(resourceExpression.length()  + identityType.length() + 2);

    resourceElement.setName(BpmnXMLConstants.ELEMENT_CUSTOM_RESOURCE);
    resourceElement.setNamespace(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE);
    resourceElement.setNamespacePrefix(BpmnXMLConstants.ACTIVITI_EXTENSIONS_PREFIX);
    
    resourceAttribute.setName(BpmnXMLConstants.ATTRIBUTE_NAME);
    resourceAttribute.setValue(resourceType);
    resourceAttribute.setNamespace(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE);
    resourceAttribute.setNamespacePrefix(BpmnXMLConstants.ACTIVITI_EXTENSIONS_PREFIX);   
    resourceElement.addAttribute(resourceAttribute);
    
    resourceAssignmentElement.setName(BpmnXMLConstants.ELEMENT_RESOURCE_ASSIGNMENT);

    formalExpressionElement.setName(BpmnXMLConstants.ELEMENT_FORMAL_EXPRESSION);
    
    finalExpression.append(identityType);
    finalExpression.append(LEFT_PAREN);
    finalExpression.append(resourceExpression);
    finalExpression.append(RIGHT_PAREN);
    formalExpressionElement.setElementText(finalExpression.toString());

    resourceAssignmentElement.addChildElement(formalExpressionElement);
    resourceElement.addChildElement(resourceAssignmentElement);
    
    return resourceElement;
  }
  
  protected void updateResourceExpression(String identityType, String resourceExpression, ExtensionElement resourceElement) {
    ExtensionElement formalExpressionElement = getFormalExpressionElement(resourceElement);
    String currentExpression = formalExpressionElement.getElementText();
    StringBuffer finalExpression = new StringBuffer(currentExpression.length() + resourceExpression.length() + identityType.length() + 4);
    
    finalExpression.append(currentExpression);
    finalExpression.append(COMMA_SPACE);
    finalExpression.append(identityType);
    finalExpression.append(LEFT_PAREN);
    finalExpression.append(resourceExpression);
    finalExpression.append(RIGHT_PAREN);
   
    formalExpressionElement.setElementText(finalExpression.toString());
  }

  public Map<String, Map<String, String>> getCustomResources(UserTask userTask) {
    Map<String, Map<String, String>> resources = new HashMap<String, Map<String, String>>();
    Map<String, List<ExtensionElement>> extensions = userTask.getExtensionElements();
    String resourceExpression = null;

    if (extensions != null) {
      // top level of resource extension - customResource element
      List<ExtensionElement> resourceElements = extensions.get(BpmnXMLConstants.ELEMENT_CUSTOM_RESOURCE);

      if (resourceElements != null) {
        String resourceType = null;
        for (ExtensionElement resourceElement : resourceElements) {
          // type of custom resource extension (e.g. administrator attribute)
          resourceType = resourceElement.getAttributeValue(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE, BpmnXMLConstants.ATTRIBUTE_NAME);
          resourceElement = getFormalExpressionElement(resourceElement);
          if (resourceElement != null) {
            resourceExpression = resourceElement.getElementText();
            resources.put(resourceType, generateResourceLists(resourceExpression));
          }
        }
      }
    }
    return resources;
  }

  protected ExtensionElement getFormalExpressionElement(ExtensionElement resourceElement) {
    if (resourceElement != null) {
      // first child - resourceAssignmentExpression element
      resourceElement = getChildElement(BpmnXMLConstants.ELEMENT_RESOURCE_ASSIGNMENT, resourceElement);
      if (resourceElement != null) {
        // second child - formalExpression element
        return getChildElement(BpmnXMLConstants.ELEMENT_FORMAL_EXPRESSION, resourceElement);
      }
    }
    return null;
  }

  protected Map<String, String> generateResourceLists(String resourceExpression) {
    Map<String, String> resourceLists = null;

    if (resourceExpression != null) {
      StringBuilder userExpression = new StringBuilder(50);
      StringBuilder groupExpression = new StringBuilder(50);
      List<String> assignmentList = CommaSplitter.splitCommas(resourceExpression);

      for (String assignmentValue : assignmentList) {
        assignmentValue = StringUtils.trimToNull(assignmentValue);
        if (assignmentValue == null) {
          continue;
        }

        if (assignmentValue.startsWith(USER)) {
          List<String> userList =
                  CommaSplitter.splitCommas(assignmentValue.substring(USER.length() + 1,
                          assignmentValue.length() - 1).trim());
          for (String user : userList) {
            if (userExpression.length() > 0) {
              userExpression.append(COMMA_SPACE);
            }
            userExpression.append(user);
          }

        } else if (assignmentValue.startsWith(GROUP)) {
          List<String> groupList =
                  CommaSplitter.splitCommas(assignmentValue.substring(GROUP.length() + 1,
                          assignmentValue.length() - 1).trim());
          for (String group : groupList) {
            if (groupExpression.length() > 0) {
              groupExpression.append(COMMA_SPACE);
            }
            groupExpression.append(group);
          }

        } else {
          if (groupExpression.length() > 0) {
            groupExpression.append(COMMA_SPACE);
          }
          groupExpression.append(assignmentValue);
        }
      }
      resourceLists = new HashMap<String, String>();
      resourceLists.put(USER, userExpression.toString());
      resourceLists.put(GROUP, groupExpression.toString());
    }
    return resourceLists;
  }

  protected ExtensionElement getChildElement(String key, ExtensionElement element) {
    if (element != null) {
      List<ExtensionElement> extensionElements = element.getChildElements().get(key);

      if (extensionElements != null) {
        return extensionElements.get(0);
      }
    }
    return null;
  }

  protected String parseCustomIdentityLink(String resourceExpression) {
    List<String> resourceList = new ArrayList();

    return convertListToCommaSeparatedString(resourceList);
  }
}
