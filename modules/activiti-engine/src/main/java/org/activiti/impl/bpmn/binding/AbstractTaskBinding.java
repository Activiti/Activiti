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
package org.activiti.impl.bpmn.binding;

import java.util.List;

import org.activiti.ActivitiException;
import org.activiti.impl.bpmn.parser.BpmnParse;
import org.activiti.impl.task.TaskDefinition;
import org.activiti.impl.xml.Element;


/**
 * Superclass for all BPMN 2.0 task type bindings (manual task, usertask, etc).
 * 
 * Note: a bpmn 2.0 'task' is not the same as a 'human task', but it is
 *       a common superType for all task type activities.
 * 
 * @author Joram Barrez
 */
public abstract class AbstractTaskBinding extends BaseElementBinding {
  
  protected static final String HUMAN_PERFORMER = "humanPerformer";
  protected static final String POTENTIAL_OWNER = "potentialOwner";
  
  protected static final String RESOURCE_ASSIGNMENT_EXPR = "resourceAssignmentExpression";
  protected static final String FORMAL_EXPRESSION = "formalExpression";
  
  protected static final String USER_PREFIX = "user(";
  protected static final String GROUP_PREFIX = "group(";
  
  protected static final String ASSIGNEE_EXTENSION = "assignee";
  protected static final String CANDIDATE_USERS_EXTENSION = "candidateUsers";
  protected static final String CANDIDATE_GROUPS_EXTENSION = "candidateGroups";

  public TaskDefinition parseTaskDefinition(Element element, BpmnParse bpmnParse) {
    TaskDefinition taskDefinition = new TaskDefinition();
    parseName(element, taskDefinition);
    taskDefinition.setDescription(parseDocumentation(element));
    parseHumanPerformer(element, taskDefinition);
    parsePotentialOwner(element, taskDefinition);
    
    // Activiti custom extension
    parseCustomExtensions(element, taskDefinition);
    
    return taskDefinition;
  }

  protected void parseName(Element taskElement, TaskDefinition taskDefinition) {
    String name = taskElement.attribute("name");
    if (name != null) {
      taskDefinition.setName(name);
    }
  }
  
  protected void parseHumanPerformer(Element taskElement, TaskDefinition taskDefinition) {
    List<Element> humanPerformerElements = taskElement.elements(HUMAN_PERFORMER);
    
    if (humanPerformerElements.size() > 1) {
      throw new ActivitiException("Invalid task definition: multiple " + HUMAN_PERFORMER 
              + " sub elements defined for " + taskDefinition.getName());
    } else if (humanPerformerElements.size() == 1) {
      Element humanPerformerElement = humanPerformerElements.get(0);
      if (humanPerformerElement != null) {
        parseHumanPerformerResourceAssignment(humanPerformerElement, taskDefinition);
      }      
    }
    
  }
  
  protected void parsePotentialOwner(Element taskElement, TaskDefinition taskDefinition) {
    List<Element> potentialOwnerElements = taskElement.elements(POTENTIAL_OWNER);
    for (Element potentialOwnerElement : potentialOwnerElements) {
      parsePotentialOwnerResourceAssignment(potentialOwnerElement, taskDefinition);
    }
  }
  
  protected void parseHumanPerformerResourceAssignment(Element performerElement, TaskDefinition taskDefinition) {
    Element raeElement = performerElement.element(RESOURCE_ASSIGNMENT_EXPR);
    if (raeElement != null) {
      Element feElement = raeElement.element(FORMAL_EXPRESSION);
      if (feElement != null) {
        taskDefinition.setAssignee(feElement.getText());
      }
    }
  }
  
  protected void parsePotentialOwnerResourceAssignment(Element performerElement, TaskDefinition taskDefinition) {
    Element raeElement = performerElement.element(RESOURCE_ASSIGNMENT_EXPR);
    if (raeElement != null) {
      Element feElement = raeElement.element(FORMAL_EXPRESSION);
      if (feElement != null) {
        String[] assignmentExpressions = splitCommaSeparatedExpression(feElement.getText());
        for (String assignmentExpression : assignmentExpressions) {
          assignmentExpression = assignmentExpression.trim();
          if (assignmentExpression.startsWith(USER_PREFIX)) {
            taskDefinition.addCandidateUserId(getAssignmentId(assignmentExpression, USER_PREFIX));
          } else if (assignmentExpression.startsWith(GROUP_PREFIX)) {
            taskDefinition.addCandidateGroupId(getAssignmentId(assignmentExpression, GROUP_PREFIX));
          } else { // default: given string is a goupId, as-is.
            taskDefinition.addCandidateGroupId(assignmentExpression);
          }
        }
      }
    }
  }
  
  protected String[] splitCommaSeparatedExpression(String expression) {
    if (expression == null) {
      throw new ActivitiException("Invalid: no content for " + FORMAL_EXPRESSION + " provided");
    }
    return expression.split(",");
  }
  
  protected String getAssignmentId(String expression, String prefix) {
    return expression.substring(prefix.length(), expression.length() - 1).trim();
  }
  
  protected void parseCustomExtensions(Element taskElement, TaskDefinition taskDefinition) {
    
    // assignee
    String assignee = taskElement.attributeNS(BpmnParse.BPMN_EXTENSIONS_NS, ASSIGNEE_EXTENSION);
    if (assignee != null) {
      if (taskDefinition.getAssignee() == null) {
        taskDefinition.setAssignee(assignee);
      } else {
        throw new ActivitiException("Invalid usage: duplicate assignee declaration for task " 
                + taskDefinition.getName());
      }
    }
    
    // Candidate users
    String candidateUsersString = taskElement.attributeNS(BpmnParse.BPMN_EXTENSIONS_NS, CANDIDATE_USERS_EXTENSION);
    if (candidateUsersString != null) {
      String[] candidateUsers = candidateUsersString.split(",");
      for (String candidateUser : candidateUsers) {
        taskDefinition.addCandidateUserId(candidateUser.trim());
      }
    }
    
    // Candidate groups
    String candidateGroupsString = taskElement.attributeNS(BpmnParse.BPMN_EXTENSIONS_NS, CANDIDATE_GROUPS_EXTENSION);
    if (candidateGroupsString != null) {
      String[] candidateGroups = candidateGroupsString.split(",");
      for (String candidateGroup : candidateGroups) {
        taskDefinition.addCandidateGroupId(candidateGroup.trim());
      }
    }
  }
  
}
