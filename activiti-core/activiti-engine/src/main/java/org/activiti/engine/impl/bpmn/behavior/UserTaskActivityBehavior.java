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
package org.activiti.engine.impl.bpmn.behavior;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.helper.SkipExpressionUtil;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.CompleteTaskCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class UserTaskActivityBehavior extends TaskActivityBehavior {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(UserTaskActivityBehavior.class);

  protected UserTask userTask;

  public UserTaskActivityBehavior(UserTask userTask) {
    this.userTask = userTask;
  }

  public void execute(DelegateExecution execution) {
    CommandContext commandContext = Context.getCommandContext();
    TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();

    TaskEntity task = taskEntityManager.create();
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    task.setExecution(executionEntity);
    task.setTaskDefinitionKey(userTask.getId());
    task.setBusinessKey(executionEntity.getProcessInstanceBusinessKey());

    String activeTaskName = null;
    String activeTaskDescription = null;
    String activeTaskDueDate = null;
    String activeTaskPriority = null;
    String activeTaskCategory = null;
    String activeTaskFormKey = null;
    String activeTaskSkipExpression = null;
    String activeTaskAssignee = null;
    String activeTaskOwner = null;
    List<String> activeTaskCandidateUsers = null;
    List<String> activeTaskCandidateGroups = null;

    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    ExpressionManager expressionManager = processEngineConfiguration.getExpressionManager();

    if (Context.getProcessEngineConfiguration().isEnableProcessDefinitionInfoCache()) {
      ObjectNode taskElementProperties = Context.getBpmnOverrideElementProperties(userTask.getId(), execution.getProcessDefinitionId());
      activeTaskName = getActiveValue(userTask.getName(), DynamicBpmnConstants.USER_TASK_NAME, taskElementProperties);
      activeTaskDescription = getActiveValue(userTask.getDocumentation(), DynamicBpmnConstants.USER_TASK_DESCRIPTION, taskElementProperties);
      activeTaskDueDate = getActiveValue(userTask.getDueDate(), DynamicBpmnConstants.USER_TASK_DUEDATE, taskElementProperties);
      activeTaskPriority = getActiveValue(userTask.getPriority(), DynamicBpmnConstants.USER_TASK_PRIORITY, taskElementProperties);
      activeTaskCategory = getActiveValue(userTask.getCategory(), DynamicBpmnConstants.USER_TASK_CATEGORY, taskElementProperties);
      activeTaskFormKey = getActiveValue(userTask.getFormKey(), DynamicBpmnConstants.USER_TASK_FORM_KEY, taskElementProperties);
      activeTaskSkipExpression = getActiveValue(userTask.getSkipExpression(), DynamicBpmnConstants.TASK_SKIP_EXPRESSION, taskElementProperties);
      activeTaskAssignee = getActiveValue(userTask.getAssignee(), DynamicBpmnConstants.USER_TASK_ASSIGNEE, taskElementProperties);
      activeTaskOwner = getActiveValue(userTask.getOwner(), DynamicBpmnConstants.USER_TASK_OWNER, taskElementProperties);
      activeTaskCandidateUsers = getActiveValueList(userTask.getCandidateUsers(), DynamicBpmnConstants.USER_TASK_CANDIDATE_USERS, taskElementProperties);
      activeTaskCandidateGroups = getActiveValueList(userTask.getCandidateGroups(), DynamicBpmnConstants.USER_TASK_CANDIDATE_GROUPS, taskElementProperties);

    } else {
      activeTaskName = userTask.getName();
      activeTaskDescription = userTask.getDocumentation();
      activeTaskDueDate = userTask.getDueDate();
      activeTaskPriority = userTask.getPriority();
      activeTaskCategory = userTask.getCategory();
      activeTaskFormKey = userTask.getFormKey();
      activeTaskSkipExpression = userTask.getSkipExpression();
      activeTaskAssignee = userTask.getAssignee();
      activeTaskOwner = userTask.getOwner();
      activeTaskCandidateUsers = userTask.getCandidateUsers();
      activeTaskCandidateGroups = userTask.getCandidateGroups();
    }

    if (StringUtils.isNotEmpty(activeTaskName)) {
      String name = null;
      try {
        name = (String) expressionManager.createExpression(activeTaskName).getValue(execution);
      } catch (ActivitiException e) {
        name = activeTaskName;
        LOGGER.warn("property not found in task name expression " + e.getMessage());
      }
      task.setName(name);
    }

    if (StringUtils.isNotEmpty(activeTaskDescription)) {
      String description = null;
      try {
        description = (String) expressionManager.createExpression(activeTaskDescription).getValue(execution);
      } catch (ActivitiException e) {
        description = activeTaskDescription;
        LOGGER.warn("property not found in task description expression " + e.getMessage());
      }
      task.setDescription(description);
    }

    if (StringUtils.isNotEmpty(activeTaskDueDate)) {
      Object dueDate = expressionManager.createExpression(activeTaskDueDate).getValue(execution);
      if (dueDate != null) {
        if (dueDate instanceof Date) {
          task.setDueDate((Date) dueDate);
        } else if (dueDate instanceof String) {
          String businessCalendarName = null;
          if (StringUtils.isNotEmpty(userTask.getBusinessCalendarName())) {
            businessCalendarName = expressionManager.createExpression(userTask.getBusinessCalendarName()).getValue(execution).toString();
          } else {
            businessCalendarName = DueDateBusinessCalendar.NAME;
          }

          BusinessCalendar businessCalendar = Context.getProcessEngineConfiguration().getBusinessCalendarManager()
              .getBusinessCalendar(businessCalendarName);
          task.setDueDate(businessCalendar.resolveDuedate((String) dueDate));

        } else {
          throw new ActivitiIllegalArgumentException("Due date expression does not resolve to a Date or Date string: " + activeTaskDueDate);
        }
      }
    }

    if (StringUtils.isNotEmpty(activeTaskPriority)) {
      final Object priority = expressionManager.createExpression(activeTaskPriority).getValue(execution);
      if (priority != null) {
        if (priority instanceof String) {
          try {
            task.setPriority(Integer.valueOf((String) priority));
          } catch (NumberFormatException e) {
            throw new ActivitiIllegalArgumentException("Priority does not resolve to a number: " + priority, e);
          }
        } else if (priority instanceof Number) {
          task.setPriority(((Number) priority).intValue());
        } else {
          throw new ActivitiIllegalArgumentException("Priority expression does not resolve to a number: " + activeTaskPriority);
        }
      }
    }

    if (StringUtils.isNotEmpty(activeTaskCategory)) {
      final Object category = expressionManager.createExpression(activeTaskCategory).getValue(execution);
      if (category != null) {
        if (category instanceof String) {
          task.setCategory((String) category);
        } else {
          throw new ActivitiIllegalArgumentException("Category expression does not resolve to a string: " + activeTaskCategory);
        }
      }
    }

    if (StringUtils.isNotEmpty(activeTaskFormKey)) {
      final Object formKey = expressionManager.createExpression(activeTaskFormKey).getValue(execution);
      if (formKey != null) {
        if (formKey instanceof String) {
          task.setFormKey((String) formKey);
        } else {
          throw new ActivitiIllegalArgumentException("FormKey expression does not resolve to a string: " + activeTaskFormKey);
        }
      }
    }

    task.setAppVersion(executionEntity.getProcessInstance().getAppVersion());

    taskEntityManager.insert(task, executionEntity);

    task.setVariablesLocal(calculateInputVariables(execution));

    boolean skipUserTask = false;
    if (StringUtils.isNotEmpty(activeTaskSkipExpression)) {
      Expression skipExpression = expressionManager.createExpression(activeTaskSkipExpression);
      skipUserTask = SkipExpressionUtil.isSkipExpressionEnabled(execution, skipExpression)
          && SkipExpressionUtil.shouldSkipFlowElement(execution, skipExpression);
    }

    // Handling assignments need to be done after the task is inserted, to have an id
    if (!skipUserTask) {
      handleAssignments(taskEntityManager, activeTaskAssignee, activeTaskOwner,
        activeTaskCandidateUsers, activeTaskCandidateGroups, task, expressionManager, execution);
    }

    processEngineConfiguration.getListenerNotificationHelper().executeTaskListeners(task, TaskListener.EVENTNAME_CREATE);

    // All properties set, now fire events
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      ActivitiEventDispatcher eventDispatcher = Context.getProcessEngineConfiguration().getEventDispatcher();
      eventDispatcher.dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_CREATED, task));
      if (task.getAssignee() != null) {
        eventDispatcher.dispatchEvent(
                ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_ASSIGNED, task));
      }
    }

    if (skipUserTask) {
      taskEntityManager.deleteTask(task, null, false, false);
      leave(execution);
    }

  }

  protected Map<String, Object> calculateInputVariables(DelegateExecution execution) {
        CommandContext commandContext = Context.getCommandContext();
        if (commandContext.getProcessEngineConfiguration().isCopyVariablesToLocalForTasks()) {
          return execution.getVariables();
        } else {
          return emptyMap();
        }
  }

  protected Map<String, Object> calculateOutBoundVariables(DelegateExecution execution,
                                                           Map<String, Object> taskVariables) {
    CommandContext commandContext = Context.getCommandContext();
      if(commandContext.getProcessEngineConfiguration().isCopyVariablesToLocalForTasks()){
          return taskVariables;
      }
      return emptyMap();
  }

  public void trigger(DelegateExecution execution, String signalName, Object signalData) {
    CommandContext commandContext = Context.getCommandContext();

    TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
    List<TaskEntity> taskEntities = taskEntityManager.findTasksByExecutionId(execution.getId()); // Should be only one
    for (TaskEntity taskEntity : taskEntities) {
      if (!taskEntity.isDeleted()) {
        throw new ActivitiException("UserTask should not be signalled before complete");
      }
    }

    propagateVariablesToProcess(execution,
                                commandContext);

    leave(execution);
  }

  private void propagateVariablesToProcess(DelegateExecution execution,
                                           CommandContext commandContext) {
    String processInstanceId = execution.getProcessInstanceId();
    ExecutionEntity processInstanceEntity = processInstanceId != null ?
            commandContext.getExecutionEntityManager().findById(processInstanceId) :
            null;

    if (processInstanceEntity != null) {
      Map<String, Object> taskVariables = new HashMap<>();

      if (commandContext.getCommand() instanceof CompleteTaskCmd) {
        taskVariables = ((CompleteTaskCmd) commandContext.getCommand()).getTaskVariables();
      }
      Map<String, Object> outboundVariables = calculateOutBoundVariables(execution,
                                                                         taskVariables);
      processInstanceEntity.setVariables(outboundVariables);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void handleAssignments(TaskEntityManager taskEntityManager, String assignee, String owner, List<String> candidateUsers,
      List<String> candidateGroups, TaskEntity task, ExpressionManager expressionManager, DelegateExecution execution) {

    if (StringUtils.isNotEmpty(assignee)) {
      Object assigneeExpressionValue = expressionManager.createExpression(assignee).getValue(execution);
      String assigneeValue = null;
      if (assigneeExpressionValue != null) {
        assigneeValue = assigneeExpressionValue.toString();
      }

      taskEntityManager.changeTaskAssigneeNoEvents(task, assigneeValue);
    }

    if (StringUtils.isNotEmpty(owner)) {
      Object ownerExpressionValue = expressionManager.createExpression(owner).getValue(execution);
      String ownerValue = null;
      if (ownerExpressionValue != null) {
        ownerValue = ownerExpressionValue.toString();
      }

      taskEntityManager.changeTaskOwner(task, ownerValue);
    }

    if (candidateGroups != null && !candidateGroups.isEmpty()) {
      for (String candidateGroup : candidateGroups) {
        Expression groupIdExpr = expressionManager.createExpression(candidateGroup);
        Object value = groupIdExpr.getValue(execution);
        if (value instanceof String) {
          List<String> candidates = extractCandidates((String) value);
          task.addCandidateGroups(candidates);
        } else if (value instanceof Collection) {
          task.addCandidateGroups((Collection) value);
        } else {
          throw new ActivitiIllegalArgumentException("Expression did not resolve to a string or collection of strings");
        }
      }
    }

    if (candidateUsers != null && !candidateUsers.isEmpty()) {
      for (String candidateUser : candidateUsers) {
        Expression userIdExpr = expressionManager.createExpression(candidateUser);
        Object value = userIdExpr.getValue(execution);
        if (value instanceof String) {
          List<String> candidates = extractCandidates((String) value);
          task.addCandidateUsers(candidates);
        } else if (value instanceof Collection) {
          task.addCandidateUsers((Collection) value);
        } else {
          throw new ActivitiException("Expression did not resolve to a string or collection of strings");
        }
      }
    }

    if (userTask.getCustomUserIdentityLinks() != null && !userTask.getCustomUserIdentityLinks().isEmpty()) {

      for (String customUserIdentityLinkType : userTask.getCustomUserIdentityLinks().keySet()) {
        for (String userIdentityLink : userTask.getCustomUserIdentityLinks().get(customUserIdentityLinkType)) {
          Expression idExpression = expressionManager.createExpression(userIdentityLink);
          Object value = idExpression.getValue(execution);
          if (value instanceof String) {
            List<String> userIds = extractCandidates((String) value);
            for (String userId : userIds) {
              task.addUserIdentityLink(userId, customUserIdentityLinkType);
            }
          } else if (value instanceof Collection) {
            Iterator userIdSet = ((Collection) value).iterator();
            while (userIdSet.hasNext()) {
              task.addUserIdentityLink((String) userIdSet.next(), customUserIdentityLinkType);
            }
          } else {
            throw new ActivitiException("Expression did not resolve to a string or collection of strings");
          }

        }
      }

    }

    if (userTask.getCustomGroupIdentityLinks() != null && !userTask.getCustomGroupIdentityLinks().isEmpty()) {

      for (String customGroupIdentityLinkType : userTask.getCustomGroupIdentityLinks().keySet()) {
        for (String groupIdentityLink : userTask.getCustomGroupIdentityLinks().get(customGroupIdentityLinkType)) {

          Expression idExpression = expressionManager.createExpression(groupIdentityLink);
          Object value = idExpression.getValue(execution);
          if (value instanceof String) {
            List<String> groupIds = extractCandidates((String) value);
            for (String groupId : groupIds) {
              task.addGroupIdentityLink(groupId, customGroupIdentityLinkType);
            }
          } else if (value instanceof Collection) {
            Iterator groupIdSet = ((Collection) value).iterator();
            while (groupIdSet.hasNext()) {
              task.addGroupIdentityLink((String) groupIdSet.next(), customGroupIdentityLinkType);
            }
          } else {
            throw new ActivitiException("Expression did not resolve to a string or collection of strings");
          }

        }
      }

    }

  }

  /**
   * Extract a candidate list from a string.
   *
   * @param str
   * @return
   */
  protected List<String> extractCandidates(String str) {
    return asList(str.split("[\\s]*,[\\s]*"));
  }
}
