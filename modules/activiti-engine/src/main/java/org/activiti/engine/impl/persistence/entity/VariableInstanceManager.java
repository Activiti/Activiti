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

import java.util.List;

import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.runtime.ByteArrayEntity;
import org.activiti.engine.impl.runtime.VariableInstanceEntity;


/**
 * @author Tom Baeyens
 */
public class VariableInstanceManager extends AbstractManager {

  public void deleteVariableInstance(VariableInstanceEntity variableInstance) {
    getPersistenceSession().delete(VariableInstanceEntity.class, variableInstance.getId());

    String byteArrayValueId = variableInstance.getByteArrayValueId();
    if (byteArrayValueId != null) {
      // the next apparently useless line is probably to ensure consistency in the DbSqlSession 
      // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
      // @see also HistoricVariableUpdateEntity
      variableInstance.getByteArrayValue();
      getPersistenceSession().delete(ByteArrayEntity.class, byteArrayValueId);
    }
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskId(String taskId) {
    return getPersistenceSession().selectList("selectVariablesByTaskId", taskId);
  }
  
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByExecutionId(String executionId) {
    return getPersistenceSession().selectList("selectVariablesByExecutionId", executionId);
  }
}
