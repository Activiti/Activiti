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

package org.activiti.engine.impl.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.cfg.TaskSession;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.task.IdentityLinkEntity;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;


/**
 * @author Tom Baeyens
 */
public class DbTaskSession implements TaskSession, Session {

  protected DbSqlSession dbSqlSession;

  public DbTaskSession() {
    this.dbSqlSession = Context.getCommandContext().getSession(DbSqlSession.class);
  }

  public TaskEntity findTaskById(String id) {
    if (id == null) {
      throw new ActivitiException("Invalid task id : null");
    }
    return (TaskEntity) dbSqlSession.selectOne("selectTask", id);
  }

  @SuppressWarnings("unchecked")
  public List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery, Page page) {
    final String query = "selectTaskByQueryCriteria";
    return dbSqlSession.selectList(query, taskQuery, page);
  }

  public long findTaskCountByQueryCriteria(TaskQueryImpl taskQuery) {
    return (Long) dbSqlSession.selectOne("selectTaskCountByQueryCriteria", taskQuery);
  }
  
  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinksByTaskId(String taskId) {
    return dbSqlSession.selectList("selectIdentityLinksByTask", taskId);
  }

  @SuppressWarnings("unchecked")
  public List<IdentityLinkEntity> findIdentityLinkByTaskUserGroupAndType(String taskId, String userId, String groupId, String type) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("taskId", taskId);
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    parameters.put("type", type);
    return dbSqlSession.selectList("selectIdentityLinkByTaskUserGroupAndType", parameters);
  }

  public long findHistoricTaskInstanceCountByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery) {
    return (Long) dbSqlSession.selectOne("selectHistoricTaskInstanceCountByQueryCriteria", historicTaskInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstance> findHistoricTaskInstancesByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery, Page page) {
    return dbSqlSession.selectList("selectHistoricTaskInstancesByQueryCriteria", historicTaskInstanceQuery, page);
  }
  
  public HistoricTaskInstanceEntity findHistoricTaskInstanceById(String id) {
    if (id == null) {
      throw new ActivitiException("Invalid historic task id : null");
    }
    return (HistoricTaskInstanceEntity) dbSqlSession.selectOne("selectHistoricTaskInstance", id);
  }
  
  public void deleteHistoricTaskInstance(String taskId) {
    HistoricTaskInstanceEntity historicTaskInstance = findHistoricTaskInstanceById(taskId);
    if(historicTaskInstance == null) {
      throw new ActivitiException("No historic task instance found for id '" + taskId + "'");
    }
    
    historicTaskInstance.delete();
  }

  @SuppressWarnings("unchecked")
  public List<Attachment> findAttachmentsByProcessInstanceId(String processInstanceId) {
    return dbSqlSession.selectList("selectAttachmentsByProcessInstanceId", processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<Attachment> findAttachmentsByTaskId(String taskId) {
    return dbSqlSession.selectList("selectAttachmentsByTaskId", taskId);
  }

  public void close() {
  }

  public void flush() {
  }
}
