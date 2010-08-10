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

package org.activiti.engine.impl.persistence.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.Page;
import org.activiti.engine.Task;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.cfg.TaskSession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.task.TaskEntity;
import org.activiti.engine.impl.persistence.task.TaskInvolvementEntity;


/**
 * @author Tom Baeyens
 */
public class DbTaskSession implements TaskSession, Session {

  protected DbSqlSession dbSqlSession;

  public DbTaskSession() {
    this.dbSqlSession = CommandContext.getCurrentSession(DbSqlSession.class);
  }

  public TaskEntity findTaskById(String id) {
    return (TaskEntity) dbSqlSession.selectOne("selectTask", id);
  }

  @SuppressWarnings("unchecked")
  public List<TaskEntity> findTasksByExecutionId(String executionId) {
    return dbSqlSession.selectList("selectTasksByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<Task> findTasksByAssignee(String assignee) {
    return dbSqlSession.selectList("selectTasksByAssignee", assignee);
  }

  @SuppressWarnings("unchecked")
  public List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery, Page page) {
    final String query = "selectTaskByQueryCriteria";
    if (page == null) {
      return dbSqlSession.selectList(query, taskQuery);
    } else {
      return dbSqlSession.selectList(query, taskQuery, page.getOffset(), page.getMaxResults());
    }
  }

  public long findTaskCountByQueryCriteria(TaskQueryImpl taskQuery) {
    return (Long) dbSqlSession.selectOne("selectTaskCountByQueryCriteria", taskQuery);
  }
  
  @SuppressWarnings("unchecked")
  public List<TaskInvolvementEntity> findTaskInvolvementsByTaskId(String taskId) {
    return dbSqlSession.selectList("selectTaskInvolvementsByTask", taskId);
  }

  @SuppressWarnings("unchecked")
  public List<Task> findCandidateTasks(String userId, List<String> groupIds) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("userId", userId);
    params.put("groupIds", groupIds);
    return dbSqlSession.selectList("selectCandidateTasks", params);
  }

  public void close() {
  }

  public void flush() {
  }

}
