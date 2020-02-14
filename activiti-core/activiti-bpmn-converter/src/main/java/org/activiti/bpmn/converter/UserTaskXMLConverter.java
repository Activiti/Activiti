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
package org.activiti.bpmn.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.child.BaseChildElementParser;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.converter.util.CommaSplitter;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CustomProperty;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Resource;
import org.activiti.bpmn.model.UserTask;
import org.activiti.bpmn.model.alfresco.AlfrescoUserTask;
import org.apache.commons.lang3.StringUtils;

/**

 */
public class UserTaskXMLConverter extends BaseBpmnXMLConverter {
  
  protected Map<String, BaseChildElementParser> childParserMap = new HashMap<String, BaseChildElementParser>();

  /** default attributes taken from bpmn spec and from activiti extension */
  protected static final List<ExtensionAttribute> defaultUserTaskAttributes = Arrays.asList(
      new ExtensionAttribute(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_FORM_FORMKEY), 
      new ExtensionAttribute(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_DUEDATE), 
      new ExtensionAttribute(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_BUSINESS_CALENDAR_NAME),
      new ExtensionAttribute(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_ASSIGNEE), 
      new ExtensionAttribute(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_OWNER), 
      new ExtensionAttribute(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_PRIORITY), 
      new ExtensionAttribute(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_CANDIDATEUSERS), 
      new ExtensionAttribute(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_CANDIDATEGROUPS),
      new ExtensionAttribute(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_CATEGORY),
      new ExtensionAttribute(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_EXTENSIONID),
      new ExtensionAttribute(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_SKIP_EXPRESSION)
  );

  public UserTaskXMLConverter() {
    HumanPerformerParser humanPerformerParser = new HumanPerformerParser();
    childParserMap.put(humanPerformerParser.getElementName(), humanPerformerParser);
    PotentialOwnerParser potentialOwnerParser = new PotentialOwnerParser();
    childParserMap.put(potentialOwnerParser.getElementName(), potentialOwnerParser);
    CustomIdentityLinkParser customIdentityLinkParser = new CustomIdentityLinkParser();
    childParserMap.put(customIdentityLinkParser.getElementName(), customIdentityLinkParser);
  }
  
  @Override
public Class<? extends BaseElement> getBpmnElementType() {
    return UserTask.class;
  }
  
  @Override
  protected String getXMLElementName() {
    return ELEMENT_TASK_USER;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
    String formKey = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_FORM_FORMKEY);
    UserTask userTask = null;
    if (StringUtils.isNotEmpty(formKey)) {
      if (model.getUserTaskFormTypes() != null && model.getUserTaskFormTypes().contains(formKey)) {
        userTask = new AlfrescoUserTask();
      }
    }
    if (userTask == null) {
      userTask = new UserTask();
    }
    BpmnXMLUtil.addXMLLocation(userTask, xtr);
    userTask.setDueDate(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_DUEDATE));
    userTask.setBusinessCalendarName(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_BUSINESS_CALENDAR_NAME));
    userTask.setCategory(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_CATEGORY));
    userTask.setFormKey(formKey);
    userTask.setAssignee(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_ASSIGNEE)); 
    userTask.setOwner(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_OWNER));
    userTask.setPriority(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_PRIORITY));
    
    if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_CANDIDATEUSERS))) {
      String expression = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_CANDIDATEUSERS);
      userTask.getCandidateUsers().addAll(parseDelimitedList(expression));
    } 
    
    if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_CANDIDATEGROUPS))) {
      String expression = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_CANDIDATEGROUPS);
      userTask.getCandidateGroups().addAll(parseDelimitedList(expression));
    }
    
    userTask.setExtensionId(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_SERVICE_EXTENSIONID));

    if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_SKIP_EXPRESSION))) {
      String expression = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_SKIP_EXPRESSION);
      userTask.setSkipExpression(expression);
    }

    BpmnXMLUtil.addCustomAttributes(xtr, userTask, defaultElementAttributes, 
        defaultActivityAttributes, defaultUserTaskAttributes);

    parseChildElements(getXMLElementName(), userTask, childParserMap, model, xtr);
    
    return userTask;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  protected void writeAdditionalAttributes(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
    UserTask userTask = (UserTask) element;
    writeQualifiedAttribute(ATTRIBUTE_TASK_USER_ASSIGNEE, userTask.getAssignee(), xtw);
    writeQualifiedAttribute(ATTRIBUTE_TASK_USER_OWNER, userTask.getOwner(), xtw);
    writeQualifiedAttribute(ATTRIBUTE_TASK_USER_CANDIDATEUSERS, convertToDelimitedString(userTask.getCandidateUsers()), xtw);
    writeQualifiedAttribute(ATTRIBUTE_TASK_USER_CANDIDATEGROUPS, convertToDelimitedString(userTask.getCandidateGroups()), xtw);
    writeQualifiedAttribute(ATTRIBUTE_TASK_USER_DUEDATE, userTask.getDueDate(), xtw);
    writeQualifiedAttribute(ATTRIBUTE_TASK_USER_BUSINESS_CALENDAR_NAME, userTask.getBusinessCalendarName(), xtw);
    writeQualifiedAttribute(ATTRIBUTE_TASK_USER_CATEGORY, userTask.getCategory(), xtw);
    writeQualifiedAttribute(ATTRIBUTE_FORM_FORMKEY, userTask.getFormKey(), xtw);
    if (userTask.getPriority() != null) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_USER_PRIORITY, userTask.getPriority().toString(), xtw);
    }
    if (StringUtils.isNotEmpty(userTask.getExtensionId())) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_SERVICE_EXTENSIONID, userTask.getExtensionId(), xtw);
    }
    if (userTask.getSkipExpression() != null) {
      writeQualifiedAttribute(ATTRIBUTE_TASK_USER_SKIP_EXPRESSION, userTask.getSkipExpression(), xtw);
    }
    // write custom attributes
    BpmnXMLUtil.writeCustomAttributes(userTask.getAttributes().values(), xtw, defaultElementAttributes, 
        defaultActivityAttributes, defaultUserTaskAttributes);
  }
  
  @Override
  protected boolean writeExtensionChildElements(BaseElement element, boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {
    UserTask userTask = (UserTask) element;
    didWriteExtensionStartElement = writeFormProperties(userTask, didWriteExtensionStartElement, xtw);
    didWriteExtensionStartElement = writeCustomIdentities(element, didWriteExtensionStartElement, xtw);
    if (!userTask.getCustomProperties().isEmpty()) {
      for (CustomProperty customProperty : userTask.getCustomProperties()) {
        
        if (StringUtils.isEmpty(customProperty.getSimpleValue())) {
          continue;
        }
        
        if (!didWriteExtensionStartElement) {
          xtw.writeStartElement(ELEMENT_EXTENSIONS);
          didWriteExtensionStartElement = true;
        }
        xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, customProperty.getName(), ACTIVITI_EXTENSIONS_NAMESPACE);
        xtw.writeCharacters(customProperty.getSimpleValue());
        xtw.writeEndElement();
      }
    }
    return didWriteExtensionStartElement;
  }
  
  protected boolean writeCustomIdentities(BaseElement element, boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {
	  UserTask userTask = (UserTask) element;
	  if (userTask.getCustomUserIdentityLinks().isEmpty() && userTask.getCustomGroupIdentityLinks().isEmpty()) {
		  return didWriteExtensionStartElement;
	  }
	    
  	if (!didWriteExtensionStartElement) { 
      xtw.writeStartElement(ELEMENT_EXTENSIONS);
      didWriteExtensionStartElement = true;
    }
  	Set<String> identityLinkTypes = new HashSet<String>();
  	identityLinkTypes.addAll(userTask.getCustomUserIdentityLinks().keySet());
  	identityLinkTypes.addAll(userTask.getCustomGroupIdentityLinks().keySet());
  	for (String identityType : identityLinkTypes) {
  		writeCustomIdentities(userTask, identityType, userTask.getCustomUserIdentityLinks().get(identityType), userTask.getCustomGroupIdentityLinks().get(identityType), xtw);
  	}
    
    return didWriteExtensionStartElement;
  }

  protected void writeCustomIdentities(UserTask userTask,String identityType, Set<String> users, Set<String> groups, XMLStreamWriter xtw) throws Exception {
	  xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_CUSTOM_RESOURCE, ACTIVITI_EXTENSIONS_NAMESPACE);
	  writeDefaultAttribute(ATTRIBUTE_NAME, identityType, xtw);
      
    List<String> identityList = new ArrayList<String>();
    
    if (users!=null) {
      for (String userId: users) {
        identityList.add("user("+userId+")");
      }
    }
    
    if (groups!=null) {
      for (String groupId: groups){
    	  identityList.add("group("+groupId+")");
      }
    }
    
    String delimitedString = convertToDelimitedString(identityList);
    
    xtw.writeStartElement(ELEMENT_RESOURCE_ASSIGNMENT);
    xtw.writeStartElement(ELEMENT_FORMAL_EXPRESSION);
    xtw.writeCharacters(delimitedString);
    xtw.writeEndElement(); // End ELEMENT_FORMAL_EXPRESSION
    xtw.writeEndElement(); // End ELEMENT_RESOURCE_ASSIGNMENT
    
    xtw.writeEndElement(); // End ELEMENT_CUSTOM_RESOURCE
  }
  
  @Override
  protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
      BpmnXMLUtil.writeIncomingAndOutgoingFlowElement((FlowNode) element, xtw);
  }
  
  public class HumanPerformerParser extends BaseChildElementParser {

    @Override
    public String getElementName() {
      return "humanPerformer";
    }

    @Override
    public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
      String resourceElement = XMLStreamReaderUtil.moveDown(xtr);
      if (StringUtils.isNotEmpty(resourceElement) && ELEMENT_RESOURCE_ASSIGNMENT.equals(resourceElement)) {
        String expression = XMLStreamReaderUtil.moveDown(xtr);
        if (StringUtils.isNotEmpty(expression) && ELEMENT_FORMAL_EXPRESSION.equals(expression)) {
          ((UserTask) parentElement).setAssignee(xtr.getElementText());
        }
      }
    }
  }

  public class PotentialOwnerParser extends BaseChildElementParser {

    @Override
    public String getElementName() {
      return "potentialOwner";
    }
    
    @Override
    public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
      String resourceElement = XMLStreamReaderUtil.moveDown(xtr);
      if (StringUtils.isNotEmpty(resourceElement) && ELEMENT_RESOURCE_ASSIGNMENT.equals(resourceElement)) {
        String expression = XMLStreamReaderUtil.moveDown(xtr);
        if (StringUtils.isNotEmpty(expression) && ELEMENT_FORMAL_EXPRESSION.equals(expression)) {
          
          List<String> assignmentList = CommaSplitter.splitCommas(xtr.getElementText());
          
          for (String assignmentValue : assignmentList) {
            if (assignmentValue == null) {
              continue;
            }
            
            assignmentValue = assignmentValue.trim();
            
            if (assignmentValue.length() == 0) {
              continue;
            }

            String userPrefix = "user(";
            String groupPrefix = "group(";
            if (assignmentValue.startsWith(userPrefix)) {
              assignmentValue = assignmentValue.substring(userPrefix.length(), assignmentValue.length() - 1).trim();
              ((UserTask) parentElement).getCandidateUsers().add(assignmentValue);
            } else if (assignmentValue.startsWith(groupPrefix)) {
              assignmentValue = assignmentValue.substring(groupPrefix.length(), assignmentValue.length() - 1).trim();
              ((UserTask) parentElement).getCandidateGroups().add(assignmentValue);
            } else {
              ((UserTask) parentElement).getCandidateGroups().add(assignmentValue);
            }
          }
        }
      } else if (StringUtils.isNotEmpty(resourceElement) && ELEMENT_RESOURCE_REF.equals(resourceElement)) {
        String resourceId = xtr.getElementText();
        if (model.containsResourceId(resourceId)) { 
          Resource resource = model.getResource(resourceId);
          ((UserTask) parentElement).getCandidateGroups().add(resource.getName());
        } else { 
          Resource resource = new Resource(resourceId, resourceId);
          model.addResource(resource);
          ((UserTask) parentElement).getCandidateGroups().add(resource.getName());
        }
      }
    }
  }
  
  public class CustomIdentityLinkParser extends BaseChildElementParser {

    @Override
    public String getElementName() {
      return ELEMENT_CUSTOM_RESOURCE;
    }
	    
    @Override
    public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
	    String identityLinkType = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_NAME);
	    
	    // the attribute value may be unqualified
	    if (identityLinkType == null) {
	      identityLinkType = xtr.getAttributeValue(null, ATTRIBUTE_NAME);
	    }
	    
	    if (identityLinkType == null) return;
	    
	    String resourceElement = XMLStreamReaderUtil.moveDown(xtr);
	      if (StringUtils.isNotEmpty(resourceElement) && ELEMENT_RESOURCE_ASSIGNMENT.equals(resourceElement)) {
	        String expression = XMLStreamReaderUtil.moveDown(xtr);
	        if (StringUtils.isNotEmpty(expression) && ELEMENT_FORMAL_EXPRESSION.equals(expression)) {
	          
	          List<String> assignmentList = CommaSplitter.splitCommas(xtr.getElementText());
	          
	          for (String assignmentValue : assignmentList) {
	            if (assignmentValue == null) {
	              continue;
	            }
	            
	            assignmentValue = assignmentValue.trim();
	            
	            if (assignmentValue.length() == 0) {
	              continue;
	            }

	            String userPrefix = "user(";
	            String groupPrefix = "group(";
	            if (assignmentValue.startsWith(userPrefix)) {
	              assignmentValue = assignmentValue.substring(userPrefix.length(), assignmentValue.length() - 1).trim();
	              ((UserTask) parentElement).addCustomUserIdentityLink(assignmentValue, identityLinkType);
	            } else if (assignmentValue.startsWith(groupPrefix)) {
	              assignmentValue = assignmentValue.substring(groupPrefix.length(), assignmentValue.length() - 1).trim();
	              ((UserTask) parentElement).addCustomGroupIdentityLink(assignmentValue, identityLinkType);
	            } else {
	              ((UserTask) parentElement).addCustomGroupIdentityLink(assignmentValue, identityLinkType);
	            }
	          }
	        }
	      }
	    }
	  }
}
