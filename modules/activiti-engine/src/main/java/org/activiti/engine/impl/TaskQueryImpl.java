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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.identity.GroupEntity;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.task.TaskQueryProperty;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;

/**
 * @author Joram Barrez
 * @author Tom Baeyens
 */
public class TaskQueryImpl extends AbstractQuery<Task> implements TaskQuery {
  
  protected String taskId;
  protected String name;
  protected String nameLike;
  protected String description;
  protected String descriptionLike;
  protected Integer priority;
  protected String assignee;
  protected String candidateUser;
  protected String candidateGroup;
  protected String processInstanceId;
  protected String executionId;
  protected TaskQueryProperty orderProperty;
  
  public TaskQueryImpl() {
  }
  
  public TaskQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public TaskQueryImpl taskId(String taskId) {
    if (taskId == null) {
      throw new ActivitiException("Task id is null");
    }
    this.taskId = taskId;
    return this;
  }
  
  public TaskQueryImpl name(String name) {
    if (name == null) {
      throw new ActivitiException("Task name is null");
    }
    this.name = name;
    return this;
  }
  
  public TaskQueryImpl nameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiException("Task namelike is null");
    }
    this.nameLike = nameLike;
    return this;
  }
  
  public TaskQueryImpl description(String description) {
    if (description == null) {
      throw new ActivitiException("Task description is null");
    }
    this.description = description;
    return this;
  }
  
  public TaskQuery descriptionLike(String descriptionLike) {
    if (descriptionLike == null) {
      throw new ActivitiException("Task descriptionlike is null");
    }
    this.descriptionLike = descriptionLike;
    return this;
  }
  
  public TaskQuery priority(Integer priority) {
    if (priority == null) {
      throw new ActivitiException("Task priority is null");
    }
    this.priority = priority;
    return this;
  }

  public TaskQueryImpl assignee(String assignee) {
    if (assignee == null) {
      throw new ActivitiException("Task assignee is null");
    }
    this.assignee = assignee;
    return this;
  }

  public TaskQueryImpl candidateUser(String candidateUser) {
    if (candidateUser == null) {
      throw new ActivitiException("Task candidateUser is null");
    }
    if (candidateGroup != null) {
      throw new ActivitiException("Invalid query usage: cannot set both candidateUser and candidateGroup");
    }
    this.candidateUser = candidateUser;
    return this;
  }
  
  public TaskQueryImpl candidateGroup(String candidateGroup) {
    if (candidateGroup == null) {
      throw new ActivitiException("Task candidateGroup is null");
    }
    if (candidateUser != null) {
      throw new ActivitiException("Invalid query usage: cannot set both candidateUser and candidateGroup");
    }
    this.candidateGroup = candidateGroup;
    return this;
  }
  
  public TaskQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiException("Process instance id is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public TaskQueryImpl executionId(String executionId) {
    if (executionId == null) {
      throw new ActivitiException("Execution id is null");
    }
    this.executionId = executionId;
    return this;
  }
  
  public List<String> getCandidateGroups() {
    if (candidateGroup!=null) {
      return Collections.singletonList(candidateGroup);
    } else if (candidateUser != null) {
      return getGroupsForCandidateUser(candidateUser);
    }
    return null;
  }
  
  protected List<String> getGroupsForCandidateUser(String candidateUser) {
    List<GroupEntity> groups = CommandContext
      .getCurrent()
      .getIdentitySession()
      .findGroupsByUser(candidateUser);
    List<String> groupIds = new ArrayList<String>();
    for (GroupEntity group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }
  
  //ordering ////////////////////////////////////////////////////////////////
  
  public TaskQuery orderByTaskId() {
    return orderBy(TaskQueryProperty.TASK_ID);
  }
  
  public TaskQuery orderByName() {
    return orderBy(TaskQueryProperty.NAME);
  }
  
  public TaskQuery orderByDescription() {
    return orderBy(TaskQueryProperty.DESCRIPTION);
  }
  
  public TaskQuery orderByPriority() {
    return orderBy(TaskQueryProperty.PRIORITY);
  }
  
  public TaskQuery orderByProcessInstanceId() {
    return orderBy(TaskQueryProperty.PROCESS_INSTANCE_ID);
  }
  
  public TaskQuery orderByExecutionId() {
    return orderBy(TaskQueryProperty.EXECUTION_ID);
  }
  
  public TaskQuery orderByAssignee() {
    return orderBy(TaskQueryProperty.ASSIGNEE);
  }
  
  public TaskQueryImpl orderBy(TaskQueryProperty property) {
    this.orderProperty = property;
    return this;
  }
  
  public TaskQueryImpl asc() {
    return direction(Direction.ASCENDING);
  }
  
  public TaskQueryImpl desc() {
    return direction(Direction.DESCENDING);
  }
  
  protected TaskQueryImpl direction(Direction direction) {
    if (orderProperty==null) {
      throw new ActivitiException("You should call any of the orderBy methods first before specifying a direction");
    }
    addOrder(orderProperty.getName(), direction.getName());
    orderProperty = null;
    return this;
  }
  
  //results ////////////////////////////////////////////////////////////////
  
  public List<Task> executeList(CommandContext commandContext, Page page) {
    checkQuery();
    return commandContext
      .getTaskSession()
      .findTasksByQueryCriteria(this, page);
  }
  
  public long executeCount(CommandContext commandContext) {
    checkQuery();
    return commandContext
      .getTaskSession()
      .findTaskCountByQueryCriteria(this);
  }
  
  protected void checkQuery() {
    if (orderProperty != null) {
      throw new ActivitiException("You should call any of the orderBy methods first before specifying a direction");
    }
  }
  
  //getters ////////////////////////////////////////////////////////////////

  public String getName() {
    return name;
  }
  public String getNameLike() {
    return nameLike;
  }
  public String getAssignee() {
    return assignee;
  }
  public String getCandidateUser() {
    return candidateUser;
  }
  public String getCandidateGroup() {
    return candidateGroup;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String getTaskId() {
    return taskId;
  }
  public String getDescription() {
    return description;
  }
  public String getDescriptionLike() {
    return descriptionLike;
  }
  public Integer getPriority() {
    return priority;
  }
  public TaskQueryProperty getOrderProperty() {
    return orderProperty;
  }
  
}
