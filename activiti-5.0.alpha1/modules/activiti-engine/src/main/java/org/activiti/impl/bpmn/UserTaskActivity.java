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

import org.activiti.activity.ActivityExecution;
import org.activiti.impl.task.TaskDefinition;
import org.activiti.impl.task.TaskImpl;


/**
 * @author Joram Barrez
 */
public class UserTaskActivity extends BpmnTaskActivityBehavior {
  
  protected TaskDefinition taskDefinition;

  public void execute(ActivityExecution execution) throws Exception {
    TaskImpl task = TaskImpl.createAndInsert();
    task.setName(taskDefinition.getName());
    task.setDescription(taskDefinition.getDescription());
    task.setExecution(execution);
    handleAssignments(task);
  }
  
  public void event(ActivityExecution execution, Object event) throws Exception {
    leave(execution);
  }
  
  protected void handleAssignments(TaskImpl task) {
    if (taskDefinition.getAssignee() != null) {
      task.setAssignee(taskDefinition.getAssignee());      
    } 
    
    if (!taskDefinition.getCandidateGroupIds().isEmpty()) {
      for (String groupId : taskDefinition.getCandidateGroupIds()) {
        task.addCandidateGroup(groupId);
      }
    } 
    
    if (!taskDefinition.getCandidateUserIds().isEmpty()) {
      for (String userId : taskDefinition.getCandidateUserIds()) {
        task.addCandidateUser(userId);
      }
    }
  }

  public void setTaskDefinition(TaskDefinition taskDefinition) {
    this.taskDefinition = taskDefinition;
  }
  
}
