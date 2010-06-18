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

import org.activiti.Page;
import org.activiti.Task;
import org.activiti.TaskQuery;
import org.activiti.TaskService;
import org.activiti.impl.cmd.AddTaskInvolvementCmd;
import org.activiti.impl.cmd.ClaimTaskCmd;
import org.activiti.impl.cmd.CompleteTaskCmd;
import org.activiti.impl.cmd.DeleteTaskCmd;
import org.activiti.impl.cmd.FindSingleTaskCmd;
import org.activiti.impl.cmd.SaveTaskCmd;
import org.activiti.impl.cmd.SetTaskPriorityCmd;
import org.activiti.impl.task.TaskImpl;
import org.activiti.impl.task.TaskInvolvementType;
import org.activiti.impl.task.TaskQueryImpl;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TaskServiceImpl extends ServiceImpl implements TaskService {

  public TaskServiceImpl(ProcessEngineImpl processEngine) {
    super(processEngine);
  }
  
  public Task newTask() {
    return new TaskImpl(null);
  }
  
  public Task newTask(String taskId) {
    return new TaskImpl(taskId);
  }
  
  public void saveTask(Task task) {
    cmdExecutor.execute(new SaveTaskCmd(task), processEngine);
  }
  
  public void deleteTask(String taskId) {
    cmdExecutor.execute(new DeleteTaskCmd(taskId), processEngine);
  }
  
  public Task findTask(String taskId) {
    return cmdExecutor.execute(new FindSingleTaskCmd(taskId), processEngine);
  }
  
  public List<Task> findAssignedTasks(String assignee) {
    return findAssignedTasks(assignee, null);
  }
  
  public List<Task> findAssignedTasks(String assignee, Page page) {
    TaskQuery query = createTaskQuery().assignee(assignee);
    if (page != null) {
      return query.pagedList(page.getOffset(), page.getMaxResults());
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
      return query.pagedList(page.getOffset(), page.getMaxResults());
    } else {
      return query.list();
    }
  }
  
  public void setAssignee(String taskId, String userId) {
    cmdExecutor.execute(new AddTaskInvolvementCmd(taskId, userId, null, 
            TaskInvolvementType.ASSIGNEE), processEngine);
  }
  
  public void addCandidateUser(String taskId, String userId) {
    cmdExecutor.execute(new AddTaskInvolvementCmd(taskId, userId, null, 
            TaskInvolvementType.CANDIDATE), processEngine);
  }
  
  public void addCandidateGroup(String taskId, String groupId) {
    cmdExecutor.execute(new AddTaskInvolvementCmd(taskId, null, groupId, 
            TaskInvolvementType.CANDIDATE), processEngine);
  }
  
  public void addUserInvolvement(String taskId, String userId, String involvementType) {
    cmdExecutor.execute(new AddTaskInvolvementCmd(taskId, userId, null, involvementType), processEngine);
  }

  public void addGroupInvolvement(String taskId, String groupId, String involvementType) {
    cmdExecutor.execute(new AddTaskInvolvementCmd(taskId, null, groupId, involvementType), processEngine);
  }
  
  public void claim(String taskId, String userId) {
    ClaimTaskCmd cmd = new ClaimTaskCmd(taskId, userId);
    cmdExecutor.execute(cmd, processEngine);
  }
  
  public void revoke(String taskId) {
    throw new UnsupportedOperationException();
  }

  public void complete(String taskId) {
    cmdExecutor.execute(new CompleteTaskCmd(taskId), processEngine);
  }

  public void setPriority(String taskId, int priority) {
    cmdExecutor.execute(new SetTaskPriorityCmd(taskId, priority) , processEngine);
  }
  
  public TaskQuery createTaskQuery() {
    return new TaskQueryImpl(processEngine);
  }
  
}
