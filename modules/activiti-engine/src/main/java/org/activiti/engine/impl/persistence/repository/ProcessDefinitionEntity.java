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
package org.activiti.engine.impl.persistence.repository;

import org.activiti.engine.impl.variable.DefaultVariableTypes;
import org.activiti.engine.impl.variable.VariableTypes;
import org.activiti.impl.db.execution.DbExecutionImpl;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.persistence.PersistentObject;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionEntity extends ProcessDefinitionImpl implements PersistentObject {

  private static final long serialVersionUID = 1L;
  
  public ProcessDefinitionEntity() {
    this(new DefaultVariableTypes());
  }

  public ProcessDefinitionEntity(VariableTypes variableTypes) {
    super(variableTypes);
  }

  public ExecutionImpl createProcessInstance() {
    DbExecutionImpl execution = new DbExecutionImpl(this);
    return execution;
  }

  public Object getPersistentState() {
    return ProcessDefinitionEntity.class;
  }
}
