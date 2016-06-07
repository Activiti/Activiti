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

import java.util.HashSet;
import java.util.Set;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
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

    // Business calendar name
    if (StringUtils.isNotEmpty(userTask.getBusinessCalendarName())) {
      taskDefinition.setBusinessCalendarNameExpression(expressionManager.createExpression(userTask.getBusinessCalendarName()));
    } else {
      taskDefinition.setBusinessCalendarNameExpression(expressionManager.createExpression(DueDateBusinessCalendar.NAME));
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

    // CustomUserIdentityLinks
    for (String customUserIdentityLinkType : userTask.getCustomUserIdentityLinks().keySet()) {
    	Set<Expression> userIdentityLinkExpression = new HashSet<Expression>();
    	for (String userIdentityLink : userTask.getCustomUserIdentityLinks().get(customUserIdentityLinkType)) {
    		userIdentityLinkExpression.add(expressionManager.createExpression(userIdentityLink));
    	}
    	taskDefinition.addCustomUserIdentityLinkExpression(customUserIdentityLinkType, userIdentityLinkExpression);
      }
    
    // CustomGroupIdentityLinks
    for (String customGroupIdentityLinkType : userTask.getCustomGroupIdentityLinks().keySet()) {
    	Set<Expression> groupIdentityLinkExpression = new HashSet<Expression>();
    	for (String groupIdentityLink : userTask.getCustomGroupIdentityLinks().get(customGroupIdentityLinkType)) {
    		groupIdentityLinkExpression.add(expressionManager.createExpression(groupIdentityLink));
    	}
    	taskDefinition.addCustomGroupIdentityLinkExpression(customGroupIdentityLinkType, groupIdentityLinkExpression);
      }

    if (StringUtils.isNotEmpty(userTask.getSkipExpression())) {
      taskDefinition.setSkipExpression(expressionManager.createExpression(userTask.getSkipExpression()));
    }
    
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
}
