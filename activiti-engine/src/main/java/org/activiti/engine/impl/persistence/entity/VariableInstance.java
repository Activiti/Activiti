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

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.variable.ValueFields;

/**

 * 
 * Generic variable class that can be reused for Activiti 6 and 5 engine
 */
@Internal
public interface VariableInstance extends ValueFields, Entity, HasRevision {
  
  void setName(String name);

  void setProcessInstanceId(String processInstanceId);

  void setExecutionId(String executionId);

  Object getValue();

  void setValue(Object value);

  String getTypeName();

  void setTypeName(String typeName);

  String getProcessInstanceId();

  String getTaskId();

  void setTaskId(String taskId);

  String getExecutionId();

}
