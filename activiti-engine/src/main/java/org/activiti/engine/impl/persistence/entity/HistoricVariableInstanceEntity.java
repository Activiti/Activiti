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

import java.util.Date;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;

/**


 */
@Internal
public interface HistoricVariableInstanceEntity extends ValueFields, HistoricVariableInstance, Entity, HasRevision {

  VariableType getVariableType();

  void setName(String name);

  void setVariableType(VariableType variableType);

  void setProcessInstanceId(String processInstanceId);

  void setTaskId(String taskId);

  void setCreateTime(Date createTime);

  void setLastUpdatedTime(Date lastUpdatedTime);

  String getExecutionId();

  void setExecutionId(String executionId);

  ByteArrayRef getByteArrayRef();
  
}
