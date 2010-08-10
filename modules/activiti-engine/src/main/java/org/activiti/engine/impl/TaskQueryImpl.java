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
import org.activiti.engine.Page;
import org.activiti.engine.SortOrder;
import org.activiti.engine.Task;
import org.activiti.engine.TaskQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.identity.GroupEntity;

/**
 * @author Joram Barrez
 * @author Tom Baeyens
 */
public class TaskQueryImpl extends AbstractQuery<Task> implements TaskQuery {
  
  protected String name;
  protected String assignee;
  protected String candidateUser;
  protected String candidateGroup;
  protected String processInstanceId;
  protected String executionId;
  
  public TaskQueryImpl() {
  }
  
  public TaskQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public TaskQueryImpl name(String name) {
    this.name = name;
    return this;
  }

  public TaskQueryImpl assignee(String assignee) {
    this.assignee = assignee;
    return this;
  }

  public TaskQueryImpl candidateUser(String candidateUser) {
    if (candidateGroup != null) {
      throw new ActivitiException("Invalid query usage: cannot set both candidateUser and candidateGroup");
    }
    this.candidateUser = candidateUser;
    return this;
  }
  
  public TaskQueryImpl candidateGroup(String candidateGroup) {
    if (candidateUser != null) {
      throw new ActivitiException("Invalid query usage: cannot set both candidateUser and candidateGroup");
    }
    this.candidateGroup = candidateGroup;
    return this;
  }
  
  public TaskQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public TaskQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }
  
  public TaskQueryImpl orderAsc(String column) {
    super.addOrder(column, SORTORDER_ASC);
    return this;
  }
  
  public TaskQueryImpl orderDesc(String column) {
    super.addOrder(column, SORTORDER_DESC);
    return this;
  }
  
  public List<Task> executeList(CommandContext commandContext, Page page) {
    return commandContext
      .getTaskSession()
      .findTasksByQueryCriteria(this, page);
  }
  
  public long executeCount(CommandContext commandContext) {
    return commandContext
      .getTaskSession()
      .findTaskCountByQueryCriteria(this);
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

  
  public String getName() {
    return name;
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

}
