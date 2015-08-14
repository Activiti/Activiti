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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.helper.SkipExpressionUtil;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.task.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * activity implementation for the user task.
 * 
 * @author Joram Barrez
 */
public class UserTaskActivityBehavior extends TaskActivityBehavior {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(UserTaskActivityBehavior.class);

  protected TaskDefinition taskDefinition;

  public UserTaskActivityBehavior(TaskDefinition taskDefinition) {
    this.taskDefinition = taskDefinition;
  }

  public void execute(ActivityExecution execution) {
    TaskEntity task = TaskEntity.createAndInsert(execution);
    task.setExecution(execution);
    task.setTaskDefinition(taskDefinition);

    if (taskDefinition.getNameExpression() != null) {
      String name = null;
      try {
        name = (String) taskDefinition.getNameExpression().getValue(execution);
      } catch (ActivitiException e) {
        name = taskDefinition.getNameExpression().getExpressionText();
        LOGGER.warn("property not found in task name expression " + e.getMessage());
      }
      task.setName(name);
    }

    if (taskDefinition.getDescriptionExpression() != null) {
      String description = null;
      try {
        description = (String) taskDefinition.getDescriptionExpression().getValue(execution);
      } catch (ActivitiException e) {
        description = taskDefinition.getDescriptionExpression().getExpressionText();
        LOGGER.warn("property not found in task description expression " + e.getMessage());
      }
      task.setDescription(description);
    }

    if (taskDefinition.getDueDateExpression() != null) {
      Object dueDate = taskDefinition.getDueDateExpression().getValue(execution);
      if (dueDate != null) {
        if (dueDate instanceof Date) {
          task.setDueDate((Date) dueDate);
        } else if (dueDate instanceof String) {
          BusinessCalendar businessCalendar = Context.getProcessEngineConfiguration().getBusinessCalendarManager().getBusinessCalendar(DueDateBusinessCalendar.NAME);
          task.setDueDate(businessCalendar.resolveDuedate((String) dueDate));
        } else {
          throw new ActivitiIllegalArgumentException("Due date expression does not resolve to a Date or Date string: " + taskDefinition.getDueDateExpression().getExpressionText());
        }
      }
    }

    if (taskDefinition.getPriorityExpression() != null) {
      final Object priority = taskDefinition.getPriorityExpression().getValue(execution);
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
          throw new ActivitiIllegalArgumentException("Priority expression does not resolve to a number: " + taskDefinition.getPriorityExpression().getExpressionText());
        }
      }
    }

    if (taskDefinition.getCategoryExpression() != null) {
      final Object category = taskDefinition.getCategoryExpression().getValue(execution);
      if (category != null) {
        if (category instanceof String) {
          task.setCategory((String) category);
        } else {
          throw new ActivitiIllegalArgumentException("Category expression does not resolve to a string: " + taskDefinition.getCategoryExpression().getExpressionText());
        }
      }
    }

    if (taskDefinition.getFormKeyExpression() != null) {
      final Object formKey = taskDefinition.getFormKeyExpression().getValue(execution);
      if (formKey != null) {
        if (formKey instanceof String) {
          task.setFormKey((String) formKey);
        } else {
          throw new ActivitiIllegalArgumentException("FormKey expression does not resolve to a string: " + taskDefinition.getFormKeyExpression().getExpressionText());
        }
      }
    }

    handleAssignments(task, execution);

    // All properties set, now firing 'create' events
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_CREATED, task));
    }

    task.fireEvent(TaskListener.EVENTNAME_CREATE);

    Expression skipExpression = taskDefinition.getSkipExpression();
    if (SkipExpressionUtil.isSkipExpressionEnabled(execution, skipExpression) && SkipExpressionUtil.shouldSkipFlowElement(execution, skipExpression)) {
      CommandContext commandContext = Context.getCommandContext();
      commandContext.getTaskEntityManager().deleteTask(task, TaskEntity.DELETE_REASON_COMPLETED, false);
      leave(execution);
    }
  }

  public void trigger(ActivityExecution execution, String signalName, Object signalData) {
    
    TaskEntityManager taskEntityManager = Context.getCommandContext().getTaskEntityManager();
    List<TaskEntity> taskEntities = taskEntityManager.findTasksByExecutionId(execution.getId()); // Should be only one
    for (TaskEntity taskEntity : taskEntities) {
      if (!taskEntity.isDeleted()) {
        throw new ActivitiException("UserTask should not be signalled before complete");
      }
    }
    
    leave(execution);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void handleAssignments(TaskEntity task, ActivityExecution execution) {
    if (taskDefinition.getAssigneeExpression() != null) {
      Object assigneeExpressionValue = taskDefinition.getAssigneeExpression().getValue(execution);
      String assigneeValue = null;
      if (assigneeExpressionValue != null) {
        assigneeValue = assigneeExpressionValue.toString();
      }
      task.setAssignee(assigneeValue, true, false);
    }

    if (taskDefinition.getOwnerExpression() != null) {
      Object ownerExpressionValue = taskDefinition.getOwnerExpression().getValue(execution);
      String ownerValue = null;
      if (ownerExpressionValue != null) {
        ownerValue = ownerExpressionValue.toString();
      }
      task.setOwner(ownerValue);
    }

    if (!taskDefinition.getCandidateGroupIdExpressions().isEmpty()) {
      for (Expression groupIdExpr : taskDefinition.getCandidateGroupIdExpressions()) {
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

    if (!taskDefinition.getCandidateUserIdExpressions().isEmpty()) {
      for (Expression userIdExpr : taskDefinition.getCandidateUserIdExpressions()) {
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

    if (!taskDefinition.getCustomUserIdentityLinkExpressions().isEmpty()) {
      Map<String, Set<Expression>> identityLinks = taskDefinition.getCustomUserIdentityLinkExpressions();
      for (String identityLinkType : identityLinks.keySet()) {
        for (Expression idExpression : identityLinks.get(identityLinkType)) {
          Object value = idExpression.getValue(execution);
          if (value instanceof String) {
            List<String> userIds = extractCandidates((String) value);
            for (String userId : userIds) {
              task.addUserIdentityLink(userId, identityLinkType);
            }
          } else if (value instanceof Collection) {
            Iterator userIdSet = ((Collection) value).iterator();
            while (userIdSet.hasNext()) {
              task.addUserIdentityLink((String) userIdSet.next(), identityLinkType);
            }
          } else {
            throw new ActivitiException("Expression did not resolve to a string or collection of strings");
          }
        }
      }
    }

    if (!taskDefinition.getCustomGroupIdentityLinkExpressions().isEmpty()) {
      Map<String, Set<Expression>> identityLinks = taskDefinition.getCustomGroupIdentityLinkExpressions();
      for (String identityLinkType : identityLinks.keySet()) {
        for (Expression idExpression : identityLinks.get(identityLinkType)) {
          Object value = idExpression.getValue(execution);
          if (value instanceof String) {
            List<String> groupIds = extractCandidates((String) value);
            for (String groupId : groupIds) {
              task.addGroupIdentityLink(groupId, identityLinkType);
            }
          } else if (value instanceof Collection) {
            Iterator groupIdSet = ((Collection) value).iterator();
            while (groupIdSet.hasNext()) {
              task.addGroupIdentityLink((String) groupIdSet.next(), identityLinkType);
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
    return Arrays.asList(str.split("[\\s]*,[\\s]*"));
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public TaskDefinition getTaskDefinition() {
    return taskDefinition;
  }

}
