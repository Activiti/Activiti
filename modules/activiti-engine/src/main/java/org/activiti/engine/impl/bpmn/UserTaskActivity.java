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
package org.activiti.engine.impl.bpmn;

import java.util.Collection;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.el.Expression;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.TaskListener;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.task.TaskEntity;

/**
 * activity implementation for the user task.
 * 
 * @author Joram Barrez
 */
public class UserTaskActivity extends TaskActivity {

  protected TaskDefinition taskDefinition;
  protected ExpressionManager expressionManager;

  public UserTaskActivity(ExpressionManager expressionManager, TaskDefinition taskDefinition) {
    this.expressionManager = expressionManager;
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
      task.setAssignee((String) taskDefinition.getAssigneeExpression().getValue(execution));
    }

    if (!taskDefinition.getCandidateGroupIdExpressions().isEmpty()) {
      for (Expression groupIdExpr : taskDefinition.getCandidateGroupIdExpressions()) {
        Object value = groupIdExpr.getValue(execution);
        if (value instanceof String) {
          task.addCandidateGroup((String) value);
        } else if (value instanceof Collection) {
          task.addCandidateGroups((Collection) value);
        } else {
          throw new ActivitiException("Expression did not resolve to a string or collection of strings");
        }
      }
    }

    if (!taskDefinition.getCandidateUserIdExpressions().isEmpty()) {
      for (Expression userIdExpr : taskDefinition.getCandidateUserIdExpressions()) {
        Object value = userIdExpr.getValue(execution);
        if (value instanceof String) {
          task.addCandidateUser((String) value);
        } else if (value instanceof Collection) {
          task.addCandidateUsers((Collection) value);
        } else {
          throw new ActivitiException("Expression did not resolve to a string or collection of strings");
        }
      }
    }
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public TaskDefinition getTaskDefinition() {
    return taskDefinition;
  }
  
  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

}
