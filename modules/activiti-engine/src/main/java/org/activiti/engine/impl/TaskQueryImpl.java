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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 */
public class TaskQueryImpl extends AbstractQuery<Task> implements TaskQuery {
  
  protected String name;
  
  protected String assignee;
  
  protected String candidateUser;
  
  protected String candidateGroup;
  
  protected String processInstanceId;
  
  protected String sortColumn;
  
  protected SortOrder sortOrder;
  
  public TaskQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public TaskQuery name(String name) {
    this.name = name;
    return this;
  }

  public TaskQuery assignee(String assignee) {
    this.assignee = assignee;
    return this;
  }

  public TaskQuery candidateUser(String candidateUser) {
    if (candidateGroup != null) {
      throw new ActivitiException("Invalid query usage: cannot set both candidateUser and candidateGroup");
    }
    this.candidateUser = candidateUser;
    return this;
  }
  
  public TaskQuery candidateGroup(String candidateGroup) {
    if (candidateUser != null) {
      throw new ActivitiException("Invalid query usage: cannot set both candidateUser and candidateGroup");
    }
    this.candidateGroup = candidateGroup;
    return this;
  }
  
  public TaskQuery processInstance(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public TaskQuery orderAsc(String column) {
    if (sortColumn != null) {
      throw new ActivitiException("Invalid usage: cannot use both orderAsc and orderDesc in same query");
    }
    this.sortOrder = SortOrder.ASCENDING;
    this.sortColumn = column;
    return this;
  }
  
  public TaskQuery orderDesc(String column) {
    if (sortColumn != null) {
      throw new ActivitiException("Invalid usage: cannot use both orderAsc and orderDesc in same query");
    }
    this.sortOrder = SortOrder.DESCENDING;
    this.sortColumn = column;
    return this;
  }
  
  public List<Task> executeList(CommandContext commandContext, Page page) {
    return commandContext
      .getTaskSession()
      .dynamicFindTasks(createParamMap(), page);
  }
  
  public long executeCount(CommandContext commandContext) {
    return commandContext
      .getTaskSession()
      .dynamicFindTaskCount(createParamMap());
  }
  
  protected Map<String, Object> createParamMap() {
    Map<String, Object> params = new HashMap<String, Object>();
    if (name != null) {
      params.put("name", name);
    }
    if (assignee != null) {
      params.put("assignee", assignee);
    }
    if (candidateUser != null) {
      params.put("candidateUser", candidateUser);
      if (candidateGroup == null) {
        params.put("candidateGroups", getGroupsForCandidateUser(candidateUser));        
      } 
    }
    if (candidateGroup != null) {
      params.put("candidateGroups", Collections.singletonList(candidateGroup));
    }
    if (processInstanceId != null) {
      params.put("processInstanceId", processInstanceId);
    }
    if (sortColumn != null) {
      params.put("sortColumn", sortColumn);
      if (sortOrder.equals(SortOrder.ASCENDING)) {
        params.put("sortOrder", "asc");        
      } else {
        params.put("sortOrder", "desc");
      }
    } 
    return params;
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

}
