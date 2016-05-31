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

package org.activiti.engine.impl.persistence.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.persistence.AbstractManager;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Saeid Mirzaei
 */
public class VariableInstanceEntityManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskId(String taskId) {
    return getDbSqlSession().selectList("selectVariablesByTaskId", taskId);
  }
  
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskIds(Set<String> taskIds) {
    return getDbSqlSession().selectList("selectVariablesByTaskIds", taskIds);
  }
  
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectVariablesByExecutionId", executionId);
  }
  
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByExecutionIds(Set<String> executionIds) {
    return getDbSqlSession().selectList("selectVariablesByExecutionIds", executionIds);
  }
  
	public VariableInstanceEntity findVariableInstanceByExecutionAndName(String executionId, String variableName) {
		Map<String, String> params = new HashMap<String, String>(2);
		params.put("executionId", executionId);
		params.put("name", variableName);
		return (VariableInstanceEntity) getDbSqlSession().selectOne("selectVariableInstanceByExecutionAndName", params);
	}
	
	@SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByExecutionAndNames(String executionId, Collection<String> names) {
		Map<String, Object> params = new HashMap<String, Object>(2);
		params.put("executionId", executionId);
		params.put("names", names);
		return getDbSqlSession().selectList("selectVariableInstancesByExecutionAndNames", params);
	}
	
	public VariableInstanceEntity findVariableInstanceByTaskAndName(String taskId, String variableName) {
		Map<String, String> params = new HashMap<String, String>(2);
		params.put("taskId", taskId);
		params.put("name", variableName);
		return (VariableInstanceEntity) getDbSqlSession().selectOne("selectVariableInstanceByTaskAndName", params);
	}
	
	@SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskAndNames(String taskId, Collection<String> names) {
		Map<String, Object> params = new HashMap<String, Object>(2);
		params.put("taskId", taskId);
		params.put("names", names);
		return getDbSqlSession().selectList("selectVariableInstancesByTaskAndNames", params);
	}
	
  public void deleteVariableInstanceByTask(TaskEntity task) {
    Map<String, VariableInstanceEntity> variableInstances = task.getVariableInstanceEntities();
    if (variableInstances!=null) {
      for (VariableInstanceEntity variableInstance: variableInstances.values()) {
        variableInstance.delete();
      }
    }
  }
}
