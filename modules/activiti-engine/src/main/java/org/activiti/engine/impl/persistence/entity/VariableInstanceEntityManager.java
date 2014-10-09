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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.AbstractManager;


/**
 * @author Tom Baeyens
 */
public class VariableInstanceEntityManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskId(String taskId) {
    return getDbSqlSession().selectList("selectVariablesByTaskId", taskId);
  }
  
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectVariablesByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public Long variablesCount(String executionId) {
    return (Long) getDbSqlSession().selectOne("selectVariablesCount", executionId);
  }

  @SuppressWarnings("unchecked")
  public VariableInstanceEntity findVariableInstanceByName(String executionId, String variableName) {
    Map<String, String>  params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("name", variableName);
    return (VariableInstanceEntity) getDbSqlSession().selectOne("selectVariableInstanceByName", params); 
  }

  public void deleteVariableInstanceByTask(TaskEntity task) {
    Map<String, VariableInstanceEntity> variableInstances = task.getVariableInstances();
    if (variableInstances!=null) {
      for (VariableInstanceEntity variableInstance: variableInstances.values()) {
        variableInstance.delete();
      }
    }
  }
}
