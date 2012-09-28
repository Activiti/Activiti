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
package org.activiti.editor.language.bpmn.export;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class UserTaskExport extends BaseActivityExport {

  protected String getElementName() {
    return "userTask";
  }

  protected void writeAdditionalAttributes(ObjectNode objectNode, 
      IndentingXMLStreamWriter xtw, ObjectNode modelNode) throws Exception {
    
    String assignee = null;
    String candidateUsers = null;
    String candidateGroups = null;
    JsonNode assignmentPropertyNode = getProperty(PROPERTY_USERTASK_ASSIGNMENT, objectNode);
    if (assignmentPropertyNode != null) {
      JsonNode itemsArrayNode = assignmentPropertyNode.get(EDITOR_PROPERTIES_GENERAL_ITEMS);
      if (itemsArrayNode != null) {
        for (JsonNode assignmentNode : itemsArrayNode) {
          JsonNode assignmentTypeNode = assignmentNode.get(PROPERTY_USERTASK_ASSIGNMENT_TYPE);
          JsonNode assignmentExpressionNode = assignmentNode.get(PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION);
          if (assignmentTypeNode != null && assignmentExpressionNode != null) {
            String assignmentType = assignmentTypeNode.asText();
            if (PROPERTY_USERTASK_ASSIGNEE.equalsIgnoreCase(assignmentType)) {
              assignee = assignmentExpressionNode.asText();
            } else if (PROPERTY_USERTASK_CANDIDATE_USERS.equalsIgnoreCase(assignmentType)) {
              candidateUsers = assignmentExpressionNode.asText();
            } else if (PROPERTY_USERTASK_CANDIDATE_GROUPS.equalsIgnoreCase(assignmentType)) {
              candidateGroups = assignmentExpressionNode.asText();
            }
          }
        }
      }
    }
    
    if (StringUtils.isNotEmpty(assignee)) {
    	writeQualifiedAttribute(PROPERTY_USERTASK_ASSIGNEE, assignee, xtw);
    }
    
    if (StringUtils.isNotEmpty(candidateUsers)) {
    	writeQualifiedAttribute(PROPERTY_USERTASK_CANDIDATE_USERS, candidateUsers, xtw);
    }
    
    if (StringUtils.isNotEmpty(candidateGroups)) {
    	writeQualifiedAttribute(PROPERTY_USERTASK_CANDIDATE_GROUPS, candidateGroups, xtw);
    }
    
    if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_DUEDATE, objectNode))) {
    	writeQualifiedAttribute("dueDate", getPropertyValueAsString(PROPERTY_DUEDATE, objectNode), xtw);
    }
    
    if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_PRIORITY, objectNode))) {
    	writeQualifiedAttribute("priority", getPropertyValueAsString(PROPERTY_PRIORITY, objectNode), xtw);
    }
    
    if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_FORMKEY, objectNode))) {
    	writeQualifiedAttribute("formKey", getPropertyValueAsString(PROPERTY_FORMKEY, objectNode), xtw);
    }
    
    super.writeAdditionalAttributes(objectNode, xtw, modelNode);
  }

  protected void writeAdditionalChildElements(ObjectNode objectNode, 
      IndentingXMLStreamWriter xtw, ObjectNode modelNode) throws Exception {
    
    writeFormProperties(objectNode, xtw);
    
    JsonNode listenersNode = getProperty(PROPERTY_TASK_LISTENERS, objectNode);
    if (listenersNode != null) {
      JsonNode itemsArrayNode = listenersNode.get(EDITOR_PROPERTIES_GENERAL_ITEMS);
      if (itemsArrayNode != null) {
        for (JsonNode itemNode : itemsArrayNode) {
          JsonNode typeNode = itemNode.get("task_listener_event_type");
          if (typeNode != null && StringUtils.isNotEmpty(typeNode.asText())) {
            
            if (didWriteExtensionStartElement == false) { 
              xtw.writeStartElement("extensionElements");
              didWriteExtensionStartElement = true;
            }
            
            xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, "taskListener", ACTIVITI_EXTENSIONS_NAMESPACE);
            xtw.writeAttribute("event", typeNode.asText());
            
            writeAttribute("class", "task_listener_class", itemNode, xtw);
            writeAttribute("expression", "task_listener_expression", itemNode, xtw);
            writeAttribute("delegateExpression", "task_listener_delegate_expression", itemNode, xtw);
            
            xtw.writeEndElement();
          }
        }
      }
    }
    
    super.writeAdditionalChildElements(objectNode, xtw, modelNode);
  }

}
