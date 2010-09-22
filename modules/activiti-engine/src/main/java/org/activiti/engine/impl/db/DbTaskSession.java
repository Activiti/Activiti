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

import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.cfg.TaskSession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.impl.task.IdentityLinkEntity;
import org.activiti.engine.task.Task;


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

  public void close() {
  }

  public void flush() {
  }

}
