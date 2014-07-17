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
package org.activiti.engine.impl.bpmn.parser.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.util.CommaSplitter;
import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.form.DefaultTaskFormHandler;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.apache.commons.lang3.StringUtils;


/**
 * @author Joram Barrez
 */
public class UserTaskParseHandler extends AbstractActivityBpmnParseHandler<UserTask> {
  
  public static final String PROPERTY_TASK_DEFINITION = "taskDefinition";
  private static final String USER = "user";
  private static final String GROUP = "group";
  private static final String COMMA_SPACE = ", ";
  
  public Class< ? extends BaseElement> getHandledType() {
    return UserTask.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, UserTask userTask) {
    ActivityImpl activity = createActivityOnCurrentScope(bpmnParse, userTask, BpmnXMLConstants.ELEMENT_TASK_USER);
    
    activity.setAsync(userTask.isAsynchronous());
    activity.setExclusive(!userTask.isNotExclusive()); 
    
    TaskDefinition taskDefinition = parseTaskDefinition(bpmnParse, userTask, userTask.getId(), (ProcessDefinitionEntity) bpmnParse.getCurrentScope().getProcessDefinition());
    activity.setProperty(PROPERTY_TASK_DEFINITION, taskDefinition);
    activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createUserTaskActivityBehavior(userTask, taskDefinition));
  }
  
  public TaskDefinition parseTaskDefinition(BpmnParse bpmnParse, UserTask userTask, String taskDefinitionKey, ProcessDefinitionEntity processDefinition) {
    TaskFormHandler taskFormHandler = new DefaultTaskFormHandler();
    taskFormHandler.parseConfiguration(userTask.getFormProperties(), userTask.getFormKey(), bpmnParse.getDeployment(), processDefinition);

    TaskDefinition taskDefinition = new TaskDefinition(taskFormHandler);

    taskDefinition.setKey(taskDefinitionKey);
    processDefinition.getTaskDefinitions().put(taskDefinitionKey, taskDefinition);
    ExpressionManager expressionManager = bpmnParse.getExpressionManager();

    if (StringUtils.isNotEmpty(userTask.getName())) {
      taskDefinition.setNameExpression(expressionManager.createExpression(userTask.getName()));
    }

    if (StringUtils.isNotEmpty(userTask.getDocumentation())) {
      taskDefinition.setDescriptionExpression(expressionManager.createExpression(userTask.getDocumentation()));
    }

    if (StringUtils.isNotEmpty(userTask.getAssignee())) {
      taskDefinition.setAssigneeExpression(expressionManager.createExpression(userTask.getAssignee()));
    }
    if (StringUtils.isNotEmpty(userTask.getOwner())) {
      taskDefinition.setOwnerExpression(expressionManager.createExpression(userTask.getOwner()));
    }
    for (String candidateUser : userTask.getCandidateUsers()) {
      taskDefinition.addCandidateUserIdExpression(expressionManager.createExpression(candidateUser));
    }
    for (String candidateGroup : userTask.getCandidateGroups()) {
      taskDefinition.addCandidateGroupIdExpression(expressionManager.createExpression(candidateGroup));
    }
    
    // Activiti custom extension
    
    // Task listeners
    for (ActivitiListener taskListener : userTask.getTaskListeners()) {
      taskDefinition.addTaskListener(taskListener.getEvent(), createTaskListener(bpmnParse, taskListener, userTask.getId()));
    }

    // Due date
    if (StringUtils.isNotEmpty(userTask.getDueDate())) {
      taskDefinition.setDueDateExpression(expressionManager.createExpression(userTask.getDueDate()));
    }
    
    // Category
    if (StringUtils.isNotEmpty(userTask.getCategory())) {
    	taskDefinition.setCategoryExpression(expressionManager.createExpression(userTask.getCategory()));
    }
    
    // Priority
    if (StringUtils.isNotEmpty(userTask.getPriority())) {
      taskDefinition.setPriorityExpression(expressionManager.createExpression(userTask.getPriority()));
    }
    
    if (StringUtils.isNotEmpty(userTask.getFormKey())) {
    	taskDefinition.setFormKeyExpression(expressionManager.createExpression(userTask.getFormKey()));
    }

    parseCustomIdentityLinks(bpmnParse, userTask, taskDefinition);

    return taskDefinition;
  }
  
  protected TaskListener createTaskListener(BpmnParse bpmnParse, ActivitiListener activitiListener, String taskId) {
    TaskListener taskListener = null;

    if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(activitiListener.getImplementationType())) {
      taskListener = bpmnParse.getListenerFactory().createClassDelegateTaskListener(activitiListener); 
    } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
      taskListener = bpmnParse.getListenerFactory().createExpressionTaskListener(activitiListener);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
      taskListener = bpmnParse.getListenerFactory().createDelegateExpressionTaskListener(activitiListener);
    }
    return taskListener;
  }

  protected void parseCustomIdentityLinks(BpmnParse bpmnParse, UserTask userTask, TaskDefinition taskDefinition) {
    Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
    if (extensionElements != null) {
      List<ExtensionElement> resourceLists = extensionElements.get(BpmnXMLConstants.ELEMENT_CUSTOM_RESOURCE);
      if (resourceLists != null) {
        for (ExtensionElement resourceList : resourceLists) {
          String resourceType = resourceList.getAttributeValue(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE, BpmnXMLConstants.ATTRIBUTE_NAME);
          Map<String, String> resources = getResources(resourceType, userTask);

          if (resources != null) {
            // check for user type resources
            Set<Expression> resourceExpression = convertToExpressions(false, bpmnParse, resources.get(USER));
            if (resourceExpression != null) {
              taskDefinition.addCustomUserIdentityLinkExpression(resourceType, resourceExpression);
            }

            // check for group type resources
            resourceExpression = convertToExpressions(true, bpmnParse, resources.get(GROUP));
            if (resourceExpression != null) {
              taskDefinition.addCustomGroupIdentityLinkExpression(resourceType, resourceExpression);
            }
          }
        }
      }
    }
  }

  protected Set<Expression> convertToExpressions(boolean isGroup, BpmnParse bpmnParse, String resourceExpression) {
    ExpressionManager expressionManager = bpmnParse.getExpressionManager();

    Set<Expression> resources = new HashSet<Expression>();
    List<String> resourceList = CommaSplitter.splitCommas(resourceExpression);

    if (null != resourceList) {
      for (String resource : resourceList) {
        resources.add(expressionManager.createExpression(resource));
      }
    }

    return resources;
  }

  protected Map<String, String> getResources(String resourceType, UserTask userTask) {
    Map<String, List<ExtensionElement>> extensions = userTask.getExtensionElements();
    String resourceExpression = null;

    if (extensions != null) {
      // top level of resource extension - customResource element
      List<ExtensionElement> resourceElements = extensions.get(BpmnXMLConstants.ELEMENT_CUSTOM_RESOURCE);

      if (resourceElements != null) {
        for (ExtensionElement resourceElement : resourceElements) {
          // type of custom resource extension (e.g. businessAdministrator attribute)
          if (resourceType.equals(resourceElement.getAttributeValue(BpmnXMLConstants.ACTIVITI_EXTENSIONS_NAMESPACE, BpmnXMLConstants.ATTRIBUTE_NAME))) {
            if (resourceElement != null) {
              // first child - resourceAssignmentExpression element
              resourceElement = getChildElement(BpmnXMLConstants.ELEMENT_RESOURCE_ASSIGNMENT, resourceElement);
              if (resourceElement != null) {
                // second child - formalExpression element
                resourceElement = getChildElement(BpmnXMLConstants.ELEMENT_FORMAL_EXPRESSION, resourceElement);
                if (resourceElement != null) {
                  resourceExpression = resourceElement.getElementText();
                }
              }
            }
          }
        }
      }
    }

    return generateResourceLists(resourceExpression);
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
            if (StringUtils.isNotEmpty(userExpression.toString())) {
              userExpression.append(COMMA_SPACE);
            }
            userExpression.append(user);
          }
            
        } else if (assignmentValue.startsWith(GROUP)) {
          List<String> groupList =
                  CommaSplitter.splitCommas(assignmentValue.substring(GROUP.length() + 1,
                                                                      assignmentValue.length() - 1).trim());
          for (String group : groupList) {
            if (StringUtils.isNotEmpty(groupExpression.toString())) {
              groupExpression.append(COMMA_SPACE);
            }
            groupExpression.append(group);
          }
            
        } else {
          if (StringUtils.isNotEmpty(groupExpression.toString())) {
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
}
