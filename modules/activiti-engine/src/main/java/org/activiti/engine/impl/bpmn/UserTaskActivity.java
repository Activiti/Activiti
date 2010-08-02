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

import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.task.TaskDefinition;
import org.activiti.engine.impl.persistence.task.TaskEntity;
import org.activiti.pvm.activity.ActivityContext;
import org.activiti.pvm.impl.runtime.ExecutionContextImpl;

/**
 * activity implementation for the user task.
 * 
 * @author Joram Barrez
 */
public class UserTaskActivity extends TaskActivity {

  private final TaskDefinition taskDefinition;
  private final ExpressionManager expressionManager;

  public UserTaskActivity(ExpressionManager expressionManager, TaskDefinition taskDefinition) {
    this.expressionManager = expressionManager;
    this.taskDefinition = taskDefinition;
  }

  public void start(ActivityContext activityContext) throws Exception {
    TaskEntity task = TaskEntity.createAndInsert();
    task.setActivityInstance(activityContext.getActivityInstance());

    if (taskDefinition.getName() != null) {
      String name = evaluateExpression(taskDefinition.getName(), activityContext);
      task.setName(name);
    }

    if (taskDefinition.getDescription() != null) {
      String description = evaluateExpression(taskDefinition.getDescription(), activityContext);
      task.setDescription(description);
    }

    handleAssignments(task, activityContext);
  }

  public void signal(ActivityContext activityContext, String signalName, Object signalData) throws Exception {
    leave(activityContext);
  }

  protected void handleAssignments(TaskEntity task, ActivityContext activityContext) {
    if (taskDefinition.getAssignee() != null) {
      task.setAssignee(evaluateExpression(taskDefinition.getAssignee(), activityContext));
    }

    if (!taskDefinition.getCandidateGroupIds().isEmpty()) {
      for (String groupId : taskDefinition.getCandidateGroupIds()) {
        task.addCandidateGroup(evaluateExpression(groupId, activityContext));
      }
    }

    if (!taskDefinition.getCandidateUserIds().isEmpty()) {
      for (String userId : taskDefinition.getCandidateUserIds()) {
        task.addCandidateUser(evaluateExpression(userId, activityContext));
      }
    }
  }

  protected String evaluateExpression(String expr, ActivityContext activityContext) {
    // TODO move parsing of value expression to bpmn parser and only keep evaluation here
    return (String) expressionManager.createValueExpression(expr).getValue(activityContext);
  }

}
