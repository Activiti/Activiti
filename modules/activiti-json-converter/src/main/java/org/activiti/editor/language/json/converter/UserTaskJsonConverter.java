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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.bpmn.model.BaseElement;
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
    String owner = userTask.getOwner();
    String assignee = userTask.getAssignee();
    String candidateUsers = convertListToCommaSeparatedString(userTask.getCandidateUsers());
    String candidateGroups = convertListToCommaSeparatedString(userTask.getCandidateGroups());
    
    if (StringUtils.isNotEmpty(owner) || StringUtils.isNotEmpty(assignee) || StringUtils.isNotEmpty(candidateUsers) || StringUtils.isNotEmpty(candidateGroups)) {
      ObjectNode assignmentNode = objectMapper.createObjectNode();
      ArrayNode itemsNode = objectMapper.createArrayNode();
      
      if (StringUtils.isNotEmpty(owner)) {
          ObjectNode assignmentItemNode = objectMapper.createObjectNode();
          assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_TYPE, PROPERTY_USERTASK_OWNER);
          assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, owner);
          itemsNode.add(assignmentItemNode);
        }
      
      if (StringUtils.isNotEmpty(assignee)) {
        ObjectNode assignmentItemNode = objectMapper.createObjectNode();
        assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_TYPE, PROPERTY_USERTASK_ASSIGNEE);
        assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, assignee);
        itemsNode.add(assignmentItemNode);
      }
      
      if (StringUtils.isNotEmpty(candidateUsers)) {
        ObjectNode assignmentItemNode = objectMapper.createObjectNode();
        assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_TYPE, PROPERTY_USERTASK_CANDIDATE_USERS);
        assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, candidateUsers);
        itemsNode.add(assignmentItemNode);
      }
      
      if (StringUtils.isNotEmpty(candidateGroups)) {
        ObjectNode assignmentItemNode = objectMapper.createObjectNode();
        assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_TYPE, PROPERTY_USERTASK_CANDIDATE_GROUPS);
        assignmentItemNode.put(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, candidateGroups);
        itemsNode.add(assignmentItemNode);
      }
      
      assignmentNode.put("totalCount", itemsNode.size());
      assignmentNode.put(EDITOR_PROPERTIES_GENERAL_ITEMS, itemsNode);
      propertiesNode.put(PROPERTY_USERTASK_ASSIGNMENT, assignmentNode);
      
    }
    
    if(!userTask.getCustomUserIdentityLinks().isEmpty() || !userTask.getCustomGroupIdentityLinks().isEmpty()){
  	  ObjectNode customIdentityLinksNode = objectMapper.createObjectNode();
        ArrayNode itemsNode = objectMapper.createArrayNode();
        
        for(String linkType : userTask.getCustomUserIdentityLinks().keySet()){
        	ObjectNode identityItemNode = objectMapper.createObjectNode();
        	identityItemNode.put(PROPERTY_USERTASK_IDENTITY_TYPE, "user");
        	identityItemNode.put(PROPERTY_USERTASK_IDENTITY_LINK_TYPE, linkType);
        	String users = convertListToCommaSeparatedString(new ArrayList<String>(userTask.getCustomUserIdentityLinks().get(linkType)));
        	identityItemNode.put(PROPERTY_USERTASK_IDENTITY_LINK_EXPRESSION, users);
            itemsNode.add(identityItemNode);
        }
        
        for(String linkType : userTask.getCustomGroupIdentityLinks().keySet()){
        	ObjectNode identityItemNode = objectMapper.createObjectNode();
        	identityItemNode.put(PROPERTY_USERTASK_IDENTITY_TYPE, "group");
        	identityItemNode.put(PROPERTY_USERTASK_IDENTITY_LINK_TYPE, linkType);
        	String groups = convertListToCommaSeparatedString(new ArrayList<String>(userTask.getCustomGroupIdentityLinks().get(linkType)));
        	identityItemNode.put(PROPERTY_USERTASK_IDENTITY_LINK_EXPRESSION, groups);
            itemsNode.add(identityItemNode);
        }
        
        customIdentityLinksNode.put("totalCount", itemsNode.size());
        customIdentityLinksNode.put(EDITOR_PROPERTIES_GENERAL_ITEMS, itemsNode);
        propertiesNode.put(PROPERTY_USERTASK_CUSTOM_IDENTITY_LINKS, customIdentityLinksNode);
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
            } else if (PROPERTY_USERTASK_OWNER.equals(assignmentType)) {
              task.setOwner(assignmentItemNode.get(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION).asText());
            } else if (PROPERTY_USERTASK_CANDIDATE_USERS.equals(assignmentType)) {
              task.setCandidateUsers(getValueAsList(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, assignmentItemNode));
            } else if (PROPERTY_USERTASK_CANDIDATE_GROUPS.equals(assignmentType)) {
              task.setCandidateGroups(getValueAsList(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION, assignmentItemNode));
            }
          }
        }
      }
    }
    
    JsonNode customIdentityLinksNode = getProperty(PROPERTY_USERTASK_CUSTOM_IDENTITY_LINKS, elementNode);
    if (customIdentityLinksNode != null) {
      JsonNode itemsNode = customIdentityLinksNode.get(EDITOR_PROPERTIES_GENERAL_ITEMS);
      if (itemsNode != null) {
        Iterator<JsonNode> customIdentityLinksIterator = itemsNode.elements();
        while (customIdentityLinksIterator.hasNext()) {
          JsonNode customIdentityLinksItemNode = customIdentityLinksIterator.next();
          if (customIdentityLinksItemNode.get(PROPERTY_USERTASK_IDENTITY_TYPE) != null && customIdentityLinksItemNode.get(PROPERTY_USERTASK_IDENTITY_LINK_TYPE) != null
        		  && customIdentityLinksItemNode.get(PROPERTY_USERTASK_IDENTITY_LINK_EXPRESSION) != null) {
        	  
        	 String identityType = customIdentityLinksItemNode.get(PROPERTY_USERTASK_IDENTITY_TYPE).asText();
        	 String identityLinkType = customIdentityLinksItemNode.get(PROPERTY_USERTASK_IDENTITY_LINK_TYPE).asText();
        	 
        	 List<String> identitiesList = getValueAsList(PROPERTY_USERTASK_IDENTITY_LINK_EXPRESSION, customIdentityLinksItemNode);
        	 
        	 if ("user".equals(identityType)) {
        		 Set<String> users = task.getCustomUserIdentityLinks().get(identityLinkType);
        		 if(users == null){
        			 users = new HashSet<String>();
        			 task.getCustomUserIdentityLinks().put(identityLinkType, users);
        		 }
        		 users.addAll(identitiesList);
        	 }else if ("group".equals(identityType)) {
        		 Set<String> groups = task.getCustomGroupIdentityLinks().get(identityLinkType);
        		 if(groups == null){
        			 groups = new HashSet<String>();
        			 task.getCustomGroupIdentityLinks().put(identityLinkType, groups);
        		 }
        		 groups.addAll(identitiesList);
        	 }
          }
        }
      }
    }
    convertJsonToFormProperties(elementNode, task);
    return task;
  }
}
