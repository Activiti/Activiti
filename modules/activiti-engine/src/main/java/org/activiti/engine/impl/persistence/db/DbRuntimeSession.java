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
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.cfg.RuntimeSession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.runtime.ActivityInstanceEntity;
import org.activiti.engine.impl.persistence.runtime.ByteArrayEntity;
import org.activiti.engine.impl.persistence.runtime.JobEntity;
import org.activiti.engine.impl.persistence.runtime.ProcessInstanceEntity;
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

  public void endProcessInstance(String processInstanceId, String nonCompletionReason) {
    findProcessInstanceById(processInstanceId).end();
  }

  public ProcessInstanceEntity findProcessInstanceById(String processInstanceId) {
    return (ProcessInstanceEntity) dbSqlSession.selectOne("selectProcessInstanceById", processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<ProcessInstanceEntity> findProcessInstancesByProcessDefintionId(String processDefinitionId) {
    return dbSqlSession.selectList("selectRootExecutionsForProcessDefinition", processDefinitionId);
  }

  public ProcessInstanceEntity findSubProcessInstance(String superExecutionId) {
    return (ProcessInstanceEntity) dbSqlSession.selectOne("selectSubProcessInstanceBySuperExecutionId", superExecutionId);
  }

  public long findProcessInstanceCountByDynamicCriteria(ProcessInstanceQueryImpl processInstanceQuery) {
    return (Long) dbSqlSession.selectOne("selectProcessInstanceCountByDynamicCriteria", processInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstancesByDynamicCriteria(ProcessInstanceQueryImpl processInstanceQuery) {
    return dbSqlSession.selectList("selectProcessInstanceByDynamicCriteria", processInstanceQuery);
  }

  public void insertActivityInstance(ActivityInstanceEntity activityInstance) {
    dbSqlSession.insert(activityInstance);
  }

  public void deleteActivityInstance(String activityInstanceId) {
    dbSqlSession.delete(ActivityInstanceEntity.class, activityInstanceId);
  }

  public ActivityInstanceEntity findExecutionById(String activityInstanceId) {
    return (ActivityInstanceEntity) dbSqlSession.selectOne("selectActivityInstanceById", activityInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<ActivityInstanceEntity> findActivityInstancesByParentActivityInstanceId(String parentActivityInstanceId) {
    return dbSqlSession.selectList("ActivityInstancesByParentActivityInstanceId", parentActivityInstanceId);
  }

  public ActivityInstanceEntity findActivityInstanceByProcessInstanceIdAndActivityId(String processInstanceId, String activityId) {
    throw new UnsupportedOperationException("please implement me");
  }

  public void insertVariableInstance(VariableInstanceEntity variableInstanceEntity) {
    dbSqlSession.insert(variableInstanceEntity);
  }

  public void deleteVariableInstance(String variableInstanceId) {
    dbSqlSession.delete(VariableInstanceEntity.class, variableInstanceId);
  }

  public List<VariableInstanceEntity> findVariableInstancessByProcessInstanceId(String processInstanceId) {
    return null;
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancessByActivityInstanceId(String executionId) {
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

  public void insertJob(JobEntity job) {
    dbSqlSession.insert(job);
  }

  public void deleteJob(String jobId) {
    dbSqlSession.delete(JobEntity.class, jobId);
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
  public List<TimerEntity> findTimersByActivityInstanceId(String activityInstanceId) {
    return dbSqlSession.selectList("selectTimersByActivityInstanceId", activityInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<Job> dynamicFindJobs(Map<String, Object> params, Page page) {
    final String query = "org.activiti.persistence.selectJobByDynamicCriteria";
    if (page == null) {
      return dbSqlSession.selectList(query, params);
    } else {
      return dbSqlSession.selectList(query, params, page.getOffset(), page.getMaxResults());
    }
  }

  public long dynamicJobCount(Map<String, Object> params) {
    return (Long) dbSqlSession.selectOne("org.activiti.persistence.selectJobCountByDynamicCriteria", params);
  }

  public void close() {
  }

  public void flush() {
  }
}
