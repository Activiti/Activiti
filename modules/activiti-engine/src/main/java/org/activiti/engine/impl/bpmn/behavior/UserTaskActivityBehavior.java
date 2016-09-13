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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.helper.SkipExpressionUtil;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.task.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * activity implementation for the user task.
 * 
 * @author Joram Barrez
 */
public class UserTaskActivityBehavior extends TaskActivityBehavior {
  
  private static final long serialVersionUID = 1L;
  
  private static final Logger LOGGER = LoggerFactory.getLogger(UserTaskActivityBehavior.class);

  protected String userTaskId;
  protected TaskDefinition taskDefinition;

  public UserTaskActivityBehavior(String userTaskId, TaskDefinition taskDefinition) {
    this.userTaskId = userTaskId;
    this.taskDefinition = taskDefinition;
  }

  public void execute(ActivityExecution execution) throws Exception {
    TaskEntity task = TaskEntity.createAndInsert(execution);
    task.setExecution(execution);
    
    Expression activeNameExpression = null;
    Expression activeDescriptionExpression = null;
    Expression activeDueDateExpression = null;
    Expression activePriorityExpression = null;
    Expression activeCategoryExpression = null;
    Expression activeFormKeyExpression = null;
    Expression activeSkipExpression = null;
    Expression activeAssigneeExpression = null;
    Expression activeOwnerExpression = null;
    Set<Expression> activeCandidateUserExpressions = null;
    Set<Expression> activeCandidateGroupExpressions = null;
    
    if (Context.getProcessEngineConfiguration().isEnableProcessDefinitionInfoCache()) {
      ObjectNode taskElementProperties = Context.getBpmnOverrideElementProperties(userTaskId, execution.getProcessDefinitionId());
      activeNameExpression = getActiveValue(taskDefinition.getNameExpression(), DynamicBpmnConstants.USER_TASK_NAME, taskElementProperties);
      taskDefinition.setNameExpression(activeNameExpression);
      activeDescriptionExpression = getActiveValue(taskDefinition.getDescriptionExpression(), DynamicBpmnConstants.USER_TASK_DESCRIPTION, taskElementProperties);
      taskDefinition.setDescriptionExpression(activeDescriptionExpression);
      activeDueDateExpression = getActiveValue(taskDefinition.getDueDateExpression(), DynamicBpmnConstants.USER_TASK_DUEDATE, taskElementProperties);
      taskDefinition.setDueDateExpression(activeDueDateExpression);
      activePriorityExpression = getActiveValue(taskDefinition.getPriorityExpression(), DynamicBpmnConstants.USER_TASK_PRIORITY, taskElementProperties);
      taskDefinition.setPriorityExpression(activePriorityExpression);
      activeCategoryExpression = getActiveValue(taskDefinition.getCategoryExpression(), DynamicBpmnConstants.USER_TASK_CATEGORY, taskElementProperties);
      taskDefinition.setCategoryExpression(activeCategoryExpression);
      activeFormKeyExpression = getActiveValue(taskDefinition.getFormKeyExpression(), DynamicBpmnConstants.USER_TASK_FORM_KEY, taskElementProperties);
      taskDefinition.setFormKeyExpression(activeFormKeyExpression);
      activeSkipExpression = getActiveValue(taskDefinition.getSkipExpression(), DynamicBpmnConstants.TASK_SKIP_EXPRESSION, taskElementProperties);
      taskDefinition.setSkipExpression(activeSkipExpression);
      activeAssigneeExpression = getActiveValue(taskDefinition.getAssigneeExpression(), DynamicBpmnConstants.USER_TASK_ASSIGNEE, taskElementProperties);
      taskDefinition.setAssigneeExpression(activeAssigneeExpression);
      activeOwnerExpression = getActiveValue(taskDefinition.getOwnerExpression(), DynamicBpmnConstants.USER_TASK_OWNER, taskElementProperties);
      taskDefinition.setOwnerExpression(activeOwnerExpression);
      activeCandidateUserExpressions = getActiveValueSet(taskDefinition.getCandidateUserIdExpressions(), DynamicBpmnConstants.USER_TASK_CANDIDATE_USERS, taskElementProperties);
      taskDefinition.setCandidateUserIdExpressions(activeCandidateUserExpressions);
      activeCandidateGroupExpressions = getActiveValueSet(taskDefinition.getCandidateGroupIdExpressions(), DynamicBpmnConstants.USER_TASK_CANDIDATE_GROUPS, taskElementProperties);
      taskDefinition.setCandidateGroupIdExpressions(activeCandidateGroupExpressions);
      
    } else {
      activeNameExpression = taskDefinition.getNameExpression();
      activeDescriptionExpression = taskDefinition.getDescriptionExpression();
      activeDueDateExpression = taskDefinition.getDueDateExpression();
      activePriorityExpression = taskDefinition.getPriorityExpression();
      activeCategoryExpression = taskDefinition.getCategoryExpression();
      activeFormKeyExpression = taskDefinition.getFormKeyExpression();
      activeSkipExpression = taskDefinition.getSkipExpression();
      activeAssigneeExpression = taskDefinition.getAssigneeExpression();
      activeOwnerExpression = taskDefinition.getOwnerExpression();
      activeCandidateUserExpressions = taskDefinition.getCandidateUserIdExpressions();
      activeCandidateGroupExpressions = taskDefinition.getCandidateGroupIdExpressions();
    }
    
    task.setTaskDefinition(taskDefinition);

    if (activeNameExpression != null) {
      String name = null;
      try {
        name = (String) activeNameExpression.getValue(execution);
      } catch (ActivitiException e) {
        name = activeNameExpression.getExpressionText();
        LOGGER.warn("property not found in task name expression " + e.getMessage());
      }
      task.setName(name);
    }

    if (activeDescriptionExpression != null) {
      String description = null;
      try {
        description = (String) activeDescriptionExpression.getValue(execution);
      } catch (ActivitiException e) {
        description = activeDescriptionExpression.getExpressionText();
        LOGGER.warn("property not found in task description expression " + e.getMessage());
      }
      task.setDescription(description);
    }
    
    if (activeDueDateExpression != null) {
      Object dueDate = activeDueDateExpression.getValue(execution);
      if (dueDate != null) {
        if (dueDate instanceof Date) {
          task.setDueDate((Date) dueDate);
        } else if (dueDate instanceof String) {
          BusinessCalendar businessCalendar = Context
            .getProcessEngineConfiguration()
            .getBusinessCalendarManager()
            .getBusinessCalendar(taskDefinition.getBusinessCalendarNameExpression().getValue(execution).toString());
          task.setDueDate(businessCalendar.resolveDuedate((String) dueDate));
        } else {
          throw new ActivitiIllegalArgumentException("Due date expression does not resolve to a Date or Date string: " + 
              activeDueDateExpression.getExpressionText());
        }
      }
    }

    if (activePriorityExpression != null) {
      final Object priority = activePriorityExpression.getValue(execution);
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
          throw new ActivitiIllegalArgumentException("Priority expression does not resolve to a number: " + 
              activePriorityExpression.getExpressionText());
        }
      }
    }
    
    if (activeCategoryExpression != null) {
    	final Object category = activeCategoryExpression.getValue(execution);
    	if (category != null) {
    		if (category instanceof String) {
    			task.setCategory((String) category);
    		} else {
    			 throw new ActivitiIllegalArgumentException("Category expression does not resolve to a string: " + 
    			     activeCategoryExpression.getExpressionText());
    		}
    	}
    }
    
    if (activeFormKeyExpression != null) {
    	final Object formKey = activeFormKeyExpression.getValue(execution);
    	if (formKey != null) {
    		if (formKey instanceof String) {
    			task.setFormKey((String) formKey);
    		} else {
    		  throw new ActivitiIllegalArgumentException("FormKey expression does not resolve to a string: " + 
    		      activeFormKeyExpression.getExpressionText());
    		}
    	}
    }
    
    boolean skipUserTask = SkipExpressionUtil.isSkipExpressionEnabled(execution, activeSkipExpression) &&
        SkipExpressionUtil.shouldSkipFlowElement(execution, activeSkipExpression);
    
    if (!skipUserTask) {
      handleAssignments(activeAssigneeExpression, activeOwnerExpression, activeCandidateUserExpressions, 
        activeCandidateGroupExpressions, task, execution);
    }

    task.fireEvent(TaskListener.EVENTNAME_CREATE);

    // All properties set, now firing 'create' events
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
        ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_CREATED, task));
    }

    if (skipUserTask) {
      task.complete(null, false);
    }
  }

  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    if (!((ExecutionEntity) execution).getTasks().isEmpty())
      throw new ActivitiException("UserTask should not be signalled before complete");
    leave(execution);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void handleAssignments(Expression assigneeExpression, Expression ownerExpression, Set<Expression> candidateUserExpressions,
      Set<Expression> candidateGroupExpressions, TaskEntity task, ActivityExecution execution) {
    
    if (assigneeExpression != null) {
      Object assigneeExpressionValue = assigneeExpression.getValue(execution);
      String assigneeValue = null;
      if (assigneeExpressionValue != null) {
        assigneeValue = assigneeExpressionValue.toString();
      }
      task.setAssignee(assigneeValue, true, false);
    }
    
    if (ownerExpression != null) {
      Object ownerExpressionValue = ownerExpression.getValue(execution);
      String ownerValue = null;
      if (ownerExpressionValue != null) {
        ownerValue = ownerExpressionValue.toString();
      }
      task.setOwner(ownerValue);
    }

    if (candidateGroupExpressions != null && !candidateGroupExpressions.isEmpty()) {
      for (Expression groupIdExpr : candidateGroupExpressions) {
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

    if (candidateUserExpressions != null && !candidateUserExpressions.isEmpty()) {
      for (Expression userIdExpr : candidateUserExpressions) {
        Object value = userIdExpr.getValue(execution);
        if (value instanceof String) {
          List<String> candiates = extractCandidates((String) value);
          task.addCandidateUsers(candiates);
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
        for (Expression idExpression : identityLinks.get(identityLinkType) ) {
          Object value = idExpression.getValue(execution);
          if (value instanceof String) {
            List<String> userIds = extractCandidates((String) value);
            for (String userId : userIds) {
              task.addUserIdentityLink(userId, identityLinkType);
            }
          } else if (value instanceof Collection) {
            Iterator userIdSet = ((Collection) value).iterator();
            while (userIdSet.hasNext()) {
              task.addUserIdentityLink((String)userIdSet.next(), identityLinkType);
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
        for (Expression idExpression : identityLinks.get(identityLinkType) ) {
          Object value = idExpression.getValue(execution);
          if (value instanceof String) {
            List<String> groupIds = extractCandidates((String) value);
            for (String groupId : groupIds) {
              task.addGroupIdentityLink(groupId, identityLinkType);
            }
          } else if (value instanceof Collection) {
            Iterator groupIdSet = ((Collection) value).iterator();
            while (groupIdSet.hasNext()) {
              task.addGroupIdentityLink((String)groupIdSet.next(), identityLinkType);
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
  
  protected Expression getActiveValue(Expression originalValue, String propertyName, ObjectNode taskElementProperties) {
    Expression activeValue = originalValue;
    if (taskElementProperties != null) {
      JsonNode overrideValueNode = taskElementProperties.get(propertyName);
      if (overrideValueNode != null) {
        if (overrideValueNode.isNull()) {
          activeValue = null;
        } else {
          activeValue = Context.getProcessEngineConfiguration().getExpressionManager().createExpression(overrideValueNode.asText());
        }
      }
    }
    return activeValue;
  }
  
  protected Set<Expression> getActiveValueSet(Set<Expression> originalValues, String propertyName, ObjectNode taskElementProperties) {
    Set<Expression> activeValues = originalValues;
    if (taskElementProperties != null) {
      JsonNode overrideValuesNode = taskElementProperties.get(propertyName);
      if (overrideValuesNode != null) {
        if (overrideValuesNode.isNull() || overrideValuesNode.isArray() == false || overrideValuesNode.size() == 0) {
          activeValues = null;
        } else {
          ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
          activeValues = new HashSet<Expression>();
          for (JsonNode valueNode : overrideValuesNode) {
            activeValues.add(expressionManager.createExpression(valueNode.asText()));
          }
        }
      }
    }
    return activeValues;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public TaskDefinition getTaskDefinition() {
    return taskDefinition;
  }
  
}
