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
package org.activiti.impl.task;

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.persistence.PersistentObject;


/**
 * @author Joram Barrez
 */
public class TaskInvolvement implements Serializable, PersistentObject {
  
  private static final long serialVersionUID = 1L;
  
  protected String id;
  
  protected String type;
  
  protected String userId;
  
  protected String groupId;
  
  protected String taskId;
  protected TaskImpl task;

  public Object getPersistentState() {
    return this.type;
  }

  public static TaskInvolvement createAndInsert() {
    TaskInvolvement taskInvolvement = new TaskInvolvement();
    CommandContext
        .getCurrent()
        .getPersistenceSession()
        .insert(taskInvolvement);
    return taskInvolvement;
  }
  
  public void delete() {
    CommandContext
        .getCurrent()
        .getPersistenceSession()
        .delete(this);
    
    // TODO remove this task assignment from the task
  }

  public boolean isUser() {
    return userId != null;
  }
  
  public boolean isGroup() {
    return groupId != null;
  }
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }

  public String getUserId() {
    return userId;
  }
  
  public void setUserId(String userId) {
    if (groupId != null) {
      throw new ActivitiException("Cannot assign a userId to a task assignment that already has a groupId");
    }
    this.userId = userId;
  }
  
  public String getGroupId() {
    return groupId;
  }
  
  public void setGroupId(String groupId) {
    if (userId != null) {
      throw new ActivitiException("Cannot assign a groupId to a task assignment that already has a userId");
    }
    this.groupId = groupId;
  }
  
  String getTaskId() {
    return taskId;
  }

  void setTaskId(String taskId) {
    this.taskId = taskId;
  }
  
  public TaskImpl getTask() {
    if ( (task==null) && (taskId!=null) ) {
      this.task = CommandContext
          .getCurrent()
          .getPersistenceSession()
          .findTask(taskId);
    }
    return task;
  }

  public void setTask(TaskImpl task) {
    this.task = task;
    this.taskId = task.getId();
  }
}
