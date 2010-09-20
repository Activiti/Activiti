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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.cfg.RuntimeSession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.runtime.ByteArrayEntity;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.runtime.JobEntity;
import org.activiti.engine.impl.runtime.TimerEntity;
import org.activiti.engine.impl.runtime.VariableInstanceEntity;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * @author Joram Barrez
 * @author Tom Baeyens
 */
public class DbRuntimeSession implements Session, RuntimeSession {

  protected DbSqlSession dbSqlSession;

  public DbRuntimeSession() {
    this.dbSqlSession = CommandContext.getCurrent().getDbSqlSession();
  }

  @SuppressWarnings("unchecked")
  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    ExecutionEntity execution = findExecutionById(processInstanceId);
    
    if(execution == null) {
      throw new ActivitiException("No process instance found for id '" + processInstanceId + "'");
    }
    
    List<TaskEntity> tasks = (List) new TaskQueryImpl()
      .processInstanceId(processInstanceId)
      .executeList(CommandContext.getCurrent(), null);
    for (TaskEntity task: tasks) {
      task.delete();
    }
    
    execution.deleteCascade(deleteReason);
  }

  public ExecutionEntity findSubProcessInstanceBySuperExecutionId(String superExecutionId) {
    return (ExecutionEntity) dbSqlSession.selectOne("selectSubProcessInstanceBySuperExecutionId", superExecutionId);
  }

  public long findExecutionCountByQueryCriteria(Object executionQuery) {
    return (Long) dbSqlSession.selectOne("selectExecutionCountByQueryCriteria", executionQuery);
  }

  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findExecutionsByQueryCriteria(Object executionQuery, Page page) {
    return dbSqlSession.selectList("selectExecutionsByQueryCriteria", executionQuery, page);
  }
  
  public long findProcessInstanceCountByQueryCriteria(Object executionQuery) {
    return (Long) dbSqlSession.selectOne("selectProcessInstanceCountByQueryCriteria", executionQuery);
  }
  
  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstanceByQueryCriteria(Object executionQuery, Page page) {
    return dbSqlSession.selectList("selectProcessInstanceByQueryCriteria", executionQuery, page);
  }

  public ExecutionEntity findExecutionById(String executionId) {
    return (ExecutionEntity) dbSqlSession.selectOne("selectExecutionById", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findChildExecutionsByParentExecutionId(String parentExecutionId) {
    return dbSqlSession.selectList("selectExecutionsByParentExecutionId", parentExecutionId);
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByExecutionId(String executionId) {
    return dbSqlSession.selectList("selectVariablesByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariablesByTaskId(String taskId) {
    return dbSqlSession.selectList("selectVariablesByTaskId", taskId);
  }
  
  public void insertByteArray(ByteArrayEntity byteArrayEntity) {
    dbSqlSession.insert(byteArrayEntity);
  }

  public void deleteByteArray(String byteArrayId) {
    dbSqlSession.delete(ByteArrayEntity.class, byteArrayId);
  }

  @SuppressWarnings("unchecked")
  public byte[] getByteArrayBytes(String byteArrayId) {
   Map<String, Object> temp = (Map) dbSqlSession.selectOne("selectBytesOfByteArray", byteArrayId);
   return (byte[]) temp.get("BYTES_");
  }

  public ByteArrayEntity findByteArrayById(String byteArrayId) {
    return (ByteArrayEntity) dbSqlSession.selectOne("selectByteArrayById", byteArrayId);
  }

  public JobEntity findJobById(String jobId) {
    return (JobEntity) dbSqlSession.selectOne("selectJob", jobId);
  }

  @SuppressWarnings("unchecked")
  public List<JobEntity> findJobs() {
    return dbSqlSession.selectList("selectJobs");
  }
  
  @SuppressWarnings("unchecked")
  public List<JobEntity> findNextJobsToExecute(Page page) {
    Date now = ClockUtil.getCurrentTime();
    return dbSqlSession.selectList("selectNextJobsToExecute", now, page);
  }

  @SuppressWarnings("unchecked")
  public List<JobEntity> findLockedJobs() {
    return dbSqlSession.selectList("selectLockedJobs");
  }
  
  @SuppressWarnings("unchecked")
  public List<TimerEntity> findUnlockedTimersByDuedate(Date duedate, Page page) {
  	final String query = "selectUnlockedTimersByDuedate";
    return dbSqlSession.selectList(query, duedate, page);
  }

  @SuppressWarnings("unchecked")
  public List<TimerEntity> findTimersByExecutionId(String executionId) {
    return dbSqlSession.selectList("selectTimersByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    final String query = "org.activiti.persistence.selectJobByQueryCriteria";
    return dbSqlSession.selectList(query, jobQuery, page);
  }

  public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    return (Long) dbSqlSession.selectOne("org.activiti.persistence.selectJobCountByQueryCriteria", jobQuery);
  }

  public void close() {
  }

  public void flush() {
  }
}
