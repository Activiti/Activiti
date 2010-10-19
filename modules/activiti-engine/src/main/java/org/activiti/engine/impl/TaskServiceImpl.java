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
package org.activiti.engine.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cmd.AddIdentityLinkCmd;
import org.activiti.engine.impl.cmd.ClaimTaskCmd;
import org.activiti.engine.impl.cmd.CompleteTaskCmd;
import org.activiti.engine.impl.cmd.DeleteTaskCmd;
import org.activiti.engine.impl.cmd.GetIdentityLinksForTaskCmd;
import org.activiti.engine.impl.cmd.SaveTaskCmd;
import org.activiti.engine.impl.cmd.SetTaskPriorityCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TaskServiceImpl extends ServiceImpl implements TaskService {

  public Task newTask() {
    return newTask(null);
  }
  
  public Task newTask(String taskId) {
    TaskEntity task = TaskEntity.create();
    task.setId(taskId);
    return task;
  }
  
  public void saveTask(Task task) {
    commandExecutor.execute(new SaveTaskCmd(task));
  }
  
  public void deleteTask(String taskId) {
    commandExecutor.execute(new DeleteTaskCmd(taskId));
  }
  
  public void deleteTasks(Collection<String> taskIds) {
    commandExecutor.execute(new DeleteTaskCmd(taskIds));
  }
  
  public void setAssignee(String taskId, String userId) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, null, 
            IdentityLinkType.ASSIGNEE));
  }
  
  public void addCandidateUser(String taskId, String userId) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, null, 
            IdentityLinkType.CANDIDATE));
  }
  
  public void addCandidateGroup(String taskId, String groupId) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, null, groupId, 
            IdentityLinkType.CANDIDATE));
  }
  
  public void addUserIdentityLink(String taskId, String userId, String identityLinkeType) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, userId, null, identityLinkeType));
  }

  public void addGroupIdentityLink(String taskId, String groupId, String identityLinkType) {
    commandExecutor.execute(new AddIdentityLinkCmd(taskId, null, groupId, identityLinkType));
  }
  
  public List<IdentityLink> getIdentityLinksForTask(String taskId) {
    return commandExecutor.execute(new GetIdentityLinksForTaskCmd(taskId));
  }
  
  public void claim(String taskId, String userId) {
    ClaimTaskCmd cmd = new ClaimTaskCmd(taskId, userId);
    commandExecutor.execute(cmd);
  }

  public void complete(String taskId) {
    commandExecutor.execute(new CompleteTaskCmd(taskId, null));
  }
  
  public void complete(String taskId, Map<String, Object> variables) {
    commandExecutor.execute(new CompleteTaskCmd(taskId, variables));
  }

  public void setPriority(String taskId, int priority) {
    commandExecutor.execute(new SetTaskPriorityCmd(taskId, priority) );
  }
  
  public TaskQuery createTaskQuery() {
    return new TaskQueryImpl(commandExecutor);
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

}
