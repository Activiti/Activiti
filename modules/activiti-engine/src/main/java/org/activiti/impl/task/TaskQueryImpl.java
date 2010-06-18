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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.ActivitiException;
import org.activiti.Page;
import org.activiti.Task;
import org.activiti.TaskQuery;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.identity.GroupImpl;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.query.AbstractQuery;
import org.activiti.impl.tx.TransactionContext;

/**
 * @author Joram Barrez
 */
public class TaskQueryImpl extends AbstractQuery<Task> implements TaskQuery {
  
  protected String name;
  
  protected String assignee;
  
  protected String candidateUser;
  
  protected String candidateGroup;
  
  public TaskQueryImpl(ProcessEngineImpl processEngine) {
    super(processEngine);
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
  
  protected List<Task> executeList(TransactionContext transactionContext, Page page) {
    return transactionContext
      .getTransactionalObject(PersistenceSession.class)
      .dynamicFindTasks(createParamMap(), page);
  }
  
  protected long executeCount(TransactionContext transactionContext) {
    return transactionContext
      .getTransactionalObject(PersistenceSession.class)
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
    return params;
  }
  
  protected List<String> getGroupsForCandidateUser(String candidateUser) {
    List<GroupImpl> groups = TransactionContext
      .getCurrent()
      .getTransactionalObject(PersistenceSession.class)
      .findGroupsByUser(candidateUser);
    List<String> groupIds = new ArrayList<String>();
    for (GroupImpl group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }

}
