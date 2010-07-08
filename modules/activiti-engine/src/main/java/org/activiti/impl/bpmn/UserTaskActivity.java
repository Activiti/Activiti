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
package org.activiti.impl.bpmn;

import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.scripting.ScriptingEngines;
import org.activiti.impl.task.TaskDefinition;
import org.activiti.impl.task.TaskImpl;
import org.activiti.pvm.ActivityExecution;

/**
 * activity implementation for the user task.
 * 
 * @author Joram Barrez
 */
public class UserTaskActivity extends TaskActivity {

  private final TaskDefinition taskDefinition;
  private final ScriptingEngines scriptingEngines;

  public UserTaskActivity(ScriptingEngines scriptingEngines, TaskDefinition taskDefinition) {
    this.scriptingEngines = scriptingEngines;
    this.taskDefinition = taskDefinition;
  }

  public void execute(ActivityExecution execution) throws Exception {
    TaskImpl task = TaskImpl.createAndInsert();
    task.setExecution(execution);

    if (taskDefinition.getName() != null) {
      String name = evaluateExpression(taskDefinition.getName(), execution);
      task.setName(name);
    }

    if (taskDefinition.getDescription() != null) {
      String description = evaluateExpression(taskDefinition.getDescription(), execution);
      task.setDescription(description);
    }

    handleAssignments(task, execution);
  }

  public void event(ActivityExecution execution, Object event) throws Exception {
    leave(execution);
  }

  protected void handleAssignments(TaskImpl task, ActivityExecution execution) {
    if (taskDefinition.getAssignee() != null) {
      task.setAssignee(evaluateExpression(taskDefinition.getAssignee(), execution));
    }

    if (!taskDefinition.getCandidateGroupIds().isEmpty()) {
      for (String groupId : taskDefinition.getCandidateGroupIds()) {
        task.addCandidateGroup(evaluateExpression(groupId, execution));
      }
    }

    if (!taskDefinition.getCandidateUserIds().isEmpty()) {
      for (String userId : taskDefinition.getCandidateUserIds()) {
        task.addCandidateUser(evaluateExpression(userId, execution));
      }
    }
  }

  protected String evaluateExpression(String expr, ActivityExecution execution) {
    return (String) scriptingEngines.evaluate(expr, ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE, (ExecutionImpl) execution);
  }

}
