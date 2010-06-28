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
package org.activiti.impl;

import java.util.List;
import java.util.Map;

import org.activiti.Page;
import org.activiti.Task;
import org.activiti.TaskQuery;
import org.activiti.TaskService;
import org.activiti.impl.cmd.AddTaskInvolvementCmd;
import org.activiti.impl.cmd.ClaimTaskCmd;
import org.activiti.impl.cmd.CompleteTaskCmd;
import org.activiti.impl.cmd.DeleteTaskCmd;
import org.activiti.impl.cmd.FindSingleTaskCmd;
import org.activiti.impl.cmd.GetFormCmd;
import org.activiti.impl.cmd.SaveTaskCmd;
import org.activiti.impl.cmd.SetTaskPriorityCmd;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.task.TaskImpl;
import org.activiti.impl.task.TaskInvolvementType;
import org.activiti.impl.task.TaskQueryImpl;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TaskServiceImpl implements TaskService {

  /** must be injected with {@link #setCommandExecutor(CommandExecutor)} */
  protected CommandExecutor commandExecutor;

  public Task newTask() {
    return new TaskImpl(null);
  }
  
  public Task newTask(String taskId) {
    return new TaskImpl(taskId);
  }
  
  public void saveTask(Task task) {
    commandExecutor.execute(new SaveTaskCmd(task));
  }
  
  public void deleteTask(String taskId) {
    commandExecutor.execute(new DeleteTaskCmd(taskId));
  }
  
  public Task findTask(String taskId) {
    return commandExecutor.execute(new FindSingleTaskCmd(taskId));
  }
  
  public List<Task> findAssignedTasks(String assignee) {
    return findAssignedTasks(assignee, null);
  }
  
  public List<Task> findAssignedTasks(String assignee, Page page) {
    TaskQuery query = createTaskQuery().assignee(assignee);
    if (page != null) {
      return query.paginatedList(page.getOffset(), page.getMaxResults());
    } else {
      return query.list();
    }
  }
  
  public List<Task> findUnassignedTasks(String userId) {
    return findUnassignedTasks(userId, null);
  }
  
  public List<Task> findUnassignedTasks(String userId, Page page) {
    TaskQuery query = createTaskQuery().candidateUser(userId);
    if (page != null) {
      return query.paginatedList(page.getOffset(), page.getMaxResults());
    } else {
      return query.list();
    }
  }
  
  public void setAssignee(String taskId, String userId) {
    commandExecutor.execute(new AddTaskInvolvementCmd(taskId, userId, null, 
            TaskInvolvementType.ASSIGNEE));
  }
  
  public void addCandidateUser(String taskId, String userId) {
    commandExecutor.execute(new AddTaskInvolvementCmd(taskId, userId, null, 
            TaskInvolvementType.CANDIDATE));
  }
  
  public void addCandidateGroup(String taskId, String groupId) {
    commandExecutor.execute(new AddTaskInvolvementCmd(taskId, null, groupId, 
            TaskInvolvementType.CANDIDATE));
  }
  
  public void addUserInvolvement(String taskId, String userId, String involvementType) {
    commandExecutor.execute(new AddTaskInvolvementCmd(taskId, userId, null, involvementType));
  }

  public void addGroupInvolvement(String taskId, String groupId, String involvementType) {
    commandExecutor.execute(new AddTaskInvolvementCmd(taskId, null, groupId, involvementType));
  }
  
  public void claim(String taskId, String userId) {
    ClaimTaskCmd cmd = new ClaimTaskCmd(taskId, userId);
    commandExecutor.execute(cmd);
  }
  
  public void revoke(String taskId) {
    throw new UnsupportedOperationException();
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

  public Object getTaskForm(String taskId) {
    return commandExecutor.execute(new GetFormCmd(null, null, taskId));
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }
  
  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }
}
