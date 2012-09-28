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

import org.activiti.editor.language.bpmn.model.UserTask;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class UserTaskConverter extends BaseBpmnElementToJsonConverter {

  protected String getActivityType() {
    return STENCIL_TASK_USER;
  }
  
  protected void convertElement(ObjectNode propertiesNode) {
    UserTask userTask = (UserTask) flowElement;
    String assignee = userTask.getAssignee();
    String candidateUsers = null;
    String candidateGroups = null;
    
    if (userTask.getCandidateUsers().size() > 0) {
      StringBuilder expressionBuilder = new StringBuilder();
      for (String candidateUser : userTask.getCandidateUsers()) {
        if (expressionBuilder.length() > 0) {
          expressionBuilder.append(",");
        } 
        expressionBuilder.append(candidateUser);
      }
      candidateUsers = expressionBuilder.toString();
    }
    
    if (userTask.getCandidateGroups().size() > 0) {
      StringBuilder expressionBuilder = new StringBuilder();
      for (String candidateGroup : userTask.getCandidateGroups()) {
        if (expressionBuilder.length() > 0) {
          expressionBuilder.append(",");
        } 
        expressionBuilder.append(candidateGroup);
      }
      candidateGroups = expressionBuilder.toString();
    }
    
    if (StringUtils.isNotEmpty(assignee) || StringUtils.isNotEmpty(candidateUsers) || StringUtils.isNotEmpty(candidateGroups)) {
      ObjectNode assignmentNode = objectMapper.createObjectNode();
      ArrayNode itemsNode = objectMapper.createArrayNode();
      
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
    
    addFormProperties(userTask.getFormProperties(), propertiesNode);
  }
}
