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
import org.activiti.engine.impl.cfg.TaskSession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.task.TaskEntity;
import org.activiti.engine.impl.persistence.task.TaskInvolvement;
import org.apache.ibatis.session.RowBounds;


/**
 * @author Tom Baeyens
 */
public class DbTaskSession implements TaskSession, Session {

  protected DbSqlSession dbSqlSession;

  public DbTaskSession() {
    this.dbSqlSession = CommandContext.getCurrentSession(DbSqlSession.class);
  }

  // tasks ////////////////////////////////////////////////////////////////////

  public TaskEntity findTask(String id) {
    TaskEntity task = (TaskEntity) dbSqlSession.selectOne("selectTask", id);
    if (task!=null) {
      task = (TaskEntity) loaded.add(task);
    }
    return task;
  }

  @SuppressWarnings("unchecked")
  public List<TaskInvolvement> findTaskInvolvementsByTask(String taskId) {
    List taskInvolvements = dbSqlSession.selectList("selectTaskInvolvementsByTask", taskId);
    return loaded.add(taskInvolvements);
  }

  @SuppressWarnings("unchecked")
  public List<TaskEntity> findTasksByExecution(String executionId) {
    List tasks = dbSqlSession.selectList("selectTaskByExecution", executionId);
    return loaded.add(tasks);
  }

  @SuppressWarnings("unchecked")
  public List<Task> findCandidateTasks(String userId, List<String> groupIds) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("userId", userId);
    params.put("groupIds", groupIds);
    List tasks = (List) dbSqlSession.selectList("selectCandidateTasks", params);
    return loaded.add(tasks);
  }

  @SuppressWarnings("unchecked")
  public List<Task> findTasksByAssignee(String assignee) {
    return dbSqlSession.selectList("selectTasksByAssignee", assignee);
  }

  @SuppressWarnings("unchecked")
  public List<Task> dynamicFindTasks(Map<String, Object> params, Page page) {
    final String query = "selectTaskByDynamicCriteria";
    if (page == null) {
      return dbSqlSession.selectList(query, params);
    } else {
      return dbSqlSession.selectList(query, params, new RowBounds(page.getOffset(), page.getMaxResults()));
    }
  }

  public long dynamicFindTaskCount(Map<String, Object> params) {
    return (Long) dbSqlSession.selectOne("selectTaskCountByDynamicCriteria", params);
  }

  public void close() {
  }

  public void flush() {
  }
}
