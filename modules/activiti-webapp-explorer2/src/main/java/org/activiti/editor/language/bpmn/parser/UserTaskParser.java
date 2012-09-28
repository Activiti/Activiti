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
package org.activiti.editor.language.bpmn.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.activiti.editor.language.bpmn.model.BaseElement;
import org.activiti.editor.language.bpmn.model.UserTask;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class UserTaskParser extends BaseBpmnElementParser {
  
  public UserTaskParser() {
    HumanPerformerParser humanPerformerParser = new HumanPerformerParser();
    childElementParsers.put(humanPerformerParser.getElementName(), humanPerformerParser);
    PotentialOwnerParser potentialOwnerParser = new PotentialOwnerParser();
    childElementParsers.put(potentialOwnerParser.getElementName(), potentialOwnerParser);
  }
  
  public static String getElementName() {
    return "userTask";
  }

  protected BaseElement parseElement() {
    UserTask userTask = new UserTask();
    userTask.setDueDate(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "dueDate"));
    userTask.setFormKey(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "formKey"));
    userTask.setAssignee(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "assignee")); 
    
    if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "candidateUsers"))) {
      String expression = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "candidateUsers");
      String[] expressionList = null;
      if (expression.contains(";")) {
        expressionList = expression.split(";");
      } else {
        expressionList = new String[] { expression };
      }
      for (String user : expressionList) {
        userTask.getCandidateUsers().add(user);
      }
    } 
    
    if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "candidateGroups"))) {
      String expression = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "candidateGroups");
      String[] expressionList = null;
      if (expression.contains(";")) {
        expressionList = expression.split(";");
      } else {
        expressionList = new String[] { expression };
      }
      for (String group : expressionList) {
        userTask.getCandidateGroups().add(group);
      }
    }

    if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "priority"))) {
      Integer priorityValue = null;
      try {
        priorityValue = Integer.valueOf(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, "priority"));
      } catch (Exception e) {
      }
      userTask.setPriority(priorityValue);
    }
    
    parseChildElements(getElementName(), userTask);
    
    return userTask;
  }
  
  public class HumanPerformerParser extends BaseChildElementParser {

    public String getElementName() {
      return "humanPerformer";
    }

    public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement) throws Exception {
      String resourceElement = XMLStreamReaderUtil.moveDown(xtr);
      if (StringUtils.isNotEmpty(resourceElement) && "resourceAssignmentExpression".equals(resourceElement)) {
        String expression = XMLStreamReaderUtil.moveDown(xtr);
        if (StringUtils.isNotEmpty(expression) && "formalExpression".equals(expression)) {
          ((UserTask) parentElement).setAssignee(xtr.getElementText());
        }
      }
    }
  }
  
  public class PotentialOwnerParser extends BaseChildElementParser {

    public String getElementName() {
      return "potentialOwner";
    }

    public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement) throws Exception {
      String resourceElement = XMLStreamReaderUtil.moveDown(xtr);
      if (StringUtils.isNotEmpty(resourceElement) && "resourceAssignmentExpression".equals(resourceElement)) {
        String expression = XMLStreamReaderUtil.moveDown(xtr);
        if (StringUtils.isNotEmpty(expression) && "formalExpression".equals(expression)) {
          List<String> assignmentList = new ArrayList<String>();
          String assignmentText = xtr.getElementText();
          if (assignmentText.contains(",")) {
            String[] assignmentArray = assignmentText.split(",");
            assignmentList = Arrays.asList(assignmentArray);
          } else {
            assignmentList.add(assignmentText);
          }
          for (String assignmentValue : assignmentList) {
            if (assignmentValue == null)
              continue;
            assignmentValue = assignmentValue.trim();
            if (assignmentValue.length() == 0)
              continue;

            if (assignmentValue.trim().startsWith("user(")) {
              ((UserTask) parentElement).getCandidateUsers().add(assignmentValue);

            } else {
              ((UserTask) parentElement).getCandidateGroups().add(assignmentValue);
            }
          }
        }
      }
    }
  }
}
