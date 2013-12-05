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
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.task.TaskDefinition;

/**
 * activity implementation for the user task.
 * 
 * @author Joram Barrez
 */
public class UserTaskActivityBehavior extends TaskActivityBehavior {

  protected TaskDefinition taskDefinition;

  public UserTaskActivityBehavior(TaskDefinition taskDefinition) {
    this.taskDefinition = taskDefinition;
  }

  public void execute(ActivityExecution execution) throws Exception {
    TaskEntity task = TaskEntity.createAndInsert(execution);
    task.setExecution(execution);
    task.setTaskDefinition(taskDefinition);

    if (taskDefinition.getNameExpression() != null) {
      String name = (String) taskDefinition.getNameExpression().getValue(execution);
      task.setName(name);
    }

    if (taskDefinition.getDescriptionExpression() != null) {
      String description = (String) taskDefinition.getDescriptionExpression().getValue(execution);
      task.setDescription(description);
    }
    
    if(taskDefinition.getDueDateExpression() != null) {
      Object dueDate = taskDefinition.getDueDateExpression().getValue(execution);
      if(dueDate != null) {
        if (dueDate instanceof Date) {
          task.setDueDate((Date) dueDate);
        } else if (dueDate instanceof String) {
          BusinessCalendar businessCalendar = Context
            .getProcessEngineConfiguration()
            .getBusinessCalendarManager()
            .getBusinessCalendar(DueDateBusinessCalendar.NAME);
          task.setDueDate(businessCalendar.resolveDuedate((String) dueDate));
        } else {
          throw new ActivitiIllegalArgumentException("Due date expression does not resolve to a Date or Date string: " + 
              taskDefinition.getDueDateExpression().getExpressionText());
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
          throw new ActivitiIllegalArgumentException("Priority expression does not resolve to a number: " + 
                  taskDefinition.getPriorityExpression().getExpressionText());
        }
      }
    }
    
    if (taskDefinition.getCategoryExpression() != null) {
    	final Object category = taskDefinition.getCategoryExpression().getValue(execution);
    	if (category != null) {
    		if (category instanceof String) {
    			task.setCategory((String) category);
    		} else {
    			 throw new ActivitiIllegalArgumentException("Category expression does not resolve to a string: " + 
               taskDefinition.getCategoryExpression().getExpressionText());
    		}
    	}
    }
    
    handleAssignments(task, execution);
   
    // All properties set, now firing 'create' event
    task.fireEvent(TaskListener.EVENTNAME_CREATE);
  }

  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    leave(execution);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void handleAssignments(TaskEntity task, ActivityExecution execution) {
    if (taskDefinition.getAssigneeExpression() != null) {
      task.setAssignee((String) taskDefinition.getAssigneeExpression().getValue(execution), true, false);
    }
    
    if (taskDefinition.getOwnerExpression() != null) {
      task.setOwner((String) taskDefinition.getOwnerExpression().getValue(execution));
    }

    if (!taskDefinition.getCandidateGroupIdExpressions().isEmpty()) {
      for (Expression groupIdExpr : taskDefinition.getCandidateGroupIdExpressions()) {
        Object value = groupIdExpr.getValue(execution);
        if (value instanceof String) {
          List<String> candiates = extractCandidates((String) value);
          task.addCandidateGroups(candiates);
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
          List<String> candiates = extractCandidates((String) value);
          task.addCandidateUsers(candiates);
        } else if (value instanceof Collection) {
          task.addCandidateUsers((Collection) value);
        } else {
          throw new ActivitiException("Expression did not resolve to a string or collection of strings");
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
  
  // getters and setters //////////////////////////////////////////////////////
  
  public TaskDefinition getTaskDefinition() {
    return taskDefinition;
  }
  
}
