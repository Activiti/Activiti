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
package org.activiti.engine.impl.persistence.entity.data.impl;

import static java.util.Collections.emptyList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.TaskDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.TasksByExecutionIdMatcher;
import org.activiti.engine.task.Task;

/**

 */
public class MybatisTaskDataManager extends AbstractDataManager<TaskEntity> implements TaskDataManager {

  protected CachedEntityMatcher<TaskEntity> tasksByExecutionIdMatcher = new TasksByExecutionIdMatcher();

  public MybatisTaskDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends TaskEntity> getManagedEntityClass() {
    return TaskEntityImpl.class;
  }

  @Override
  public TaskEntity create() {
    return new TaskEntityImpl();
  }

  @Override
  public List<TaskEntity> findTasksByExecutionId(final String executionId) {
    return getList("selectTasksByExecutionId", executionId, tasksByExecutionIdMatcher, true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<TaskEntity> findTasksByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectTasksByProcessInstanceId", processInstanceId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery) {
    final String query = "selectTaskByQueryCriteria";
    return getDbSqlSession().selectList(query, taskQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Task> findTasksAndVariablesByQueryCriteria(TaskQueryImpl taskQuery) {
    final String query = "selectTaskWithVariablesByQueryCriteria";
    // paging doesn't work for combining task instances and variables due to
    // an outer join, so doing it in-memory
    if (taskQuery.getFirstResult() < 0 || taskQuery.getMaxResults() <= 0) {
      return emptyList();
    }

    int firstResult = taskQuery.getFirstResult();
    int maxResults = taskQuery.getMaxResults();

    // setting max results, limit to 20000 results for performance reasons
    if (taskQuery.getTaskVariablesLimit() != null) {
      taskQuery.setMaxResults(taskQuery.getTaskVariablesLimit());
    } else {
      taskQuery.setMaxResults(getProcessEngineConfiguration().getTaskQueryLimit());
    }
    taskQuery.setFirstResult(0);

    List<Task> instanceList = getDbSqlSession().selectListWithRawParameterWithoutFilter(query, taskQuery, taskQuery.getFirstResult(), taskQuery.getMaxResults());

    if (instanceList != null && !instanceList.isEmpty()) {
      if (firstResult > 0) {
        if (firstResult <= instanceList.size()) {
          int toIndex = firstResult + Math.min(maxResults, instanceList.size() - firstResult);
          return instanceList.subList(firstResult, toIndex);
        } else {
          return emptyList();
        }
      } else {
        int toIndex = Math.min(maxResults, instanceList.size());
        return instanceList.subList(0, toIndex);
      }
    }
    return emptyList();
  }

  @Override
  public long findTaskCountByQueryCriteria(TaskQueryImpl taskQuery) {
    return (Long) getDbSqlSession().selectOne("selectTaskCountByQueryCriteria", taskQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Task> findTasksByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectTaskByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  public long findTaskCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectTaskCountByNativeQuery", parameterMap);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Task> findTasksByParentTaskId(String parentTaskId) {
    return getDbSqlSession().selectList("selectTasksByParentTaskId", parentTaskId);
  }

  @Override
  public void updateTaskTenantIdForDeployment(String deploymentId, String newTenantId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("tenantId", newTenantId);
    getDbSqlSession().update("updateTaskTenantIdForDeployment", params);
  }

}
