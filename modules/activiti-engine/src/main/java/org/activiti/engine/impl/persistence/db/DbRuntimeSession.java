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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.Job;
import org.activiti.engine.Page;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.cfg.RuntimeSession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.runtime.ByteArrayEntity;
import org.activiti.engine.impl.persistence.runtime.ExecutionEntity;
import org.activiti.engine.impl.persistence.runtime.JobEntity;
import org.activiti.engine.impl.persistence.runtime.TimerEntity;
import org.activiti.engine.impl.persistence.runtime.VariableInstanceEntity;
import org.activiti.engine.impl.util.ClockUtil;

/**
 * @author Joram Barrez
 * @author Tom Baeyens
 */
public class DbRuntimeSession implements Session, RuntimeSession {

  protected DbSqlSession dbSqlSession;

  public DbRuntimeSession() {
    this.dbSqlSession = CommandContext.getCurrentSession(DbSqlSession.class);
  }

  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    ExecutionEntity execution = findExecutionById(processInstanceId);
    execution.deleteCascade(deleteReason);
  }

  public ExecutionEntity findSubProcessInstanceBySuperExecutionId(String superExecutionId) {
    return (ExecutionEntity) dbSqlSession.selectOne("selectSubProcessInstanceBySuperExecutionId", superExecutionId);
  }

  public long findExecutionCountByQueryCriteria(Object executionQuery) {
    return (Long) dbSqlSession.selectOne("selectExecutionCountByQueryCriteria", executionQuery);
  }

  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findExecutionsByQueryCriteria(Object executionQuery) {
    return dbSqlSession.selectList("selectExecutionsByQueryCriteria", executionQuery);
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
  public List<JobEntity> findNextJobsToExecute(int maxNrOfJobs) {
    Date now = ClockUtil.getCurrentTime();
    return dbSqlSession.selectList("selectNextJobsToExecute", now, 0, maxNrOfJobs);
  }

  @SuppressWarnings("unchecked")
  public List<JobEntity> findLockedJobs() {
    return dbSqlSession.selectList("selectLockedJobs");
  }
  
  @SuppressWarnings("unchecked")
  public List<TimerEntity> findUnlockedTimersByDuedate(Date duedate, int nrOfTimers) {
	final String query = "selectUnlockedTimersByDuedate";
	if (nrOfTimers > 0) {
		return dbSqlSession.selectList(query, duedate, 0, nrOfTimers);
	} else {
		return dbSqlSession.selectList(query, duedate);
	}
  }

  @SuppressWarnings("unchecked")
  public List<TimerEntity> findTimersByExecutionId(String executionId) {
    return dbSqlSession.selectList("selectTimersByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    final String query = "org.activiti.persistence.selectJobByQueryCriteria";
    if (page == null) {
      return dbSqlSession.selectList(query, jobQuery);
    } else {
      return dbSqlSession.selectList(query, jobQuery, page.getOffset(), page.getMaxResults());
    }
  }

  public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    return (Long) dbSqlSession.selectOne("org.activiti.persistence.selectJobCountByQueryCriteria", jobQuery);
  }

  public void close() {
  }

  public void flush() {
  }
}
