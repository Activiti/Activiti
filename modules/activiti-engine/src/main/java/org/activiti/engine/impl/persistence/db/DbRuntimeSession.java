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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.Job;
import org.activiti.engine.Page;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.runtime.ByteArrayImpl;
import org.activiti.engine.impl.persistence.runtime.JobImpl;
import org.activiti.engine.impl.persistence.runtime.TimerImpl;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.impl.variable.DeserializedObject;
import org.activiti.engine.impl.variable.VariableInstance;
import org.activiti.impl.db.execution.DbExecutionImpl;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.persistence.RuntimeSession;
import org.apache.ibatis.session.RowBounds;

/**
 * @author Joram Barrez
 * @author Tom Baeyens
 */
public class DbRuntimeSession implements Session, RuntimeSession {

  private static Logger log = Logger.getLogger(DbRuntimeSession.class.getName());

  protected DbSqlSession dbSqlSession;

  TODO fix the transaction flush (uncommented to make sure I don't forget) 
  
  protected List<DeserializedObject> deserializedObjects = new ArrayList<DeserializedObject>();
  public void addDeserializedObject(Object deserializedObject, byte[] serializedBytes, VariableInstance variableInstance) {
    deserializedObjects.add(new DeserializedObject(deserializedObject, serializedBytes, variableInstance));
  }


  public DbRuntimeSession() {
    this.dbSqlSession = CommandContext.getCurrentSession(DbSqlSession.class);
  }

  // executions ///////////////////////////////////////////////////////////////

  public DbExecutionImpl findExecution(String executionId) {
    // TODO check if this execution was already loaded
    DbExecutionImpl execution = (DbExecutionImpl)
        dbSqlSession.selectOne("selectExecution", executionId);

    if (execution!=null) {
      execution = (DbExecutionImpl) loaded.add(execution);
    }
    return execution;
  }

  @SuppressWarnings("unchecked")
  public List<DbExecutionImpl> findProcessInstancesByProcessDefintionId(String processDefinitionId) {
    List executions = dbSqlSession.selectList("selectRootExecutionsForProcessDefinition", processDefinitionId);
    return loaded.add(executions);
  }

  @SuppressWarnings("unchecked")
  public List<ExecutionImpl> findChildExecutions(String parentExecutionid) {
    List executions = dbSqlSession.selectList("selectChildExecutions", parentExecutionid);
    return loaded.add(executions);
  }

  public void deleteExecution(String executionId) {
    ExecutionImpl execution = findExecution(executionId);
    execution.end(); // TODO replace with real delete instead of end(), since this will create history traces
  }

  public DbExecutionImpl findSubProcessInstance(String superExecutionId) {
    DbExecutionImpl subProcessInstance = (DbExecutionImpl) dbSqlSession.selectOne("selectSubProcessInstanceBySuperExecutionId", superExecutionId);
    if (subProcessInstance != null) {
      subProcessInstance = (DbExecutionImpl) loaded.add(subProcessInstance);
    }
    return subProcessInstance;
  }
  
  public long findProcessInstanceCountByDynamicCriteria(Map<String, Object> params) {
    return (Long) dbSqlSession.selectOne("selectProcessInstanceCountByDynamicCriteria", params);
  }

  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstancesByDynamicCriteria(Map<String, Object> params) {
    return dbSqlSession.selectList("selectProcessInstanceByDynamicCriteria", params);
  }

  // variables ////////////////////////////////////////////////////////////////

  public List<VariableInstance> findVariablesByExecutionId(String executionId) {
    List variablesInstances = dbSqlSession.selectList("selectVariablesByExecutionId", executionId);
    loaded.add(variablesInstances);
    return variablesInstances;
  }

  public List<VariableInstance> findVariablesByTaskId(String taskId) {
    List variableInstances = dbSqlSession.selectList("selectVariablesByTaskId", taskId);
    loaded.add(variableInstances);
    return variableInstances;
  }
  
  @SuppressWarnings("unchecked")
  public byte[] getByteArrayBytes(String byteArrayId) {
   Map<String, Object> temp = (Map) dbSqlSession.selectOne("selectBytesOfByteArray", byteArrayId);
   return (byte[]) temp.get("BYTES_");
  }

  public ByteArrayImpl findByteArrayById(String byteArrayId) {
    ByteArrayImpl byteArray = (ByteArrayImpl) dbSqlSession.selectOne("selectByteArrayById", byteArrayId);
    loaded.add(byteArray);
    return byteArray;
  }


  public ProcessDefinitionImpl findProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("processDefinitionKey", processDefinitionKey);
    return (ProcessDefinitionImpl) dbSqlSession.selectOne("selectProcessDefinitionByDeploymentAndKey", parameters);
  }


  // job /////////////////////////////////////////////////////////////////////

  public JobImpl findJobById(String jobId) {
    JobImpl job = (JobImpl) dbSqlSession.selectOne("selectJob", jobId);
    if (job!=null) {
      loaded.add(job);
    }
    return job;
  }

  public List<JobImpl> findJobs() {
    return dbSqlSession.selectList("selectJobs");
  }
  
  public List<JobImpl> findNextJobsToExecute(int maxNrOfJobs) {
    Date now = ClockUtil.getCurrentTime();
    RowBounds rowBounds = new RowBounds(0, maxNrOfJobs);
    List<JobImpl> jobs = dbSqlSession.selectList("selectNextJobsToExecute", now, rowBounds);
    if (jobs!=null) {
      loaded.add(jobs);
    }
    return jobs;
  }

  @SuppressWarnings("unchecked")
  public List<JobImpl> findLockedJobs() {
    return dbSqlSession.selectList("selectLockedJobs");
  }
  
  @SuppressWarnings("unchecked")
  public List<TimerImpl> findUnlockedTimersByDuedate(Date duedate, int nrOfTimers) {
	final String query = "selectUnlockedTimersByDuedate";
	if (nrOfTimers > 0) {
		RowBounds rowBounds = new RowBounds(0,nrOfTimers);
		return dbSqlSession.selectList(statement(query), duedate, rowBounds);
	} else {
		return dbSqlSession.selectList(statement(query), duedate);
	}
  }

  @SuppressWarnings("unchecked")
  public List<TimerImpl> findTimersByExecutionId(String executionId) {
    return dbSqlSession.selectList("selectTimersByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<Job> dynamicFindJobs(Map<String, Object> params, Page page) {
    final String query = "org.activiti.persistence.selectJobByDynamicCriteria";
    if (page == null) {
      return dbSqlSession.selectList(query, params);
    } else {
      return dbSqlSession.selectList(query, params, new RowBounds(page.getOffset(), page.getMaxResults()));
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
