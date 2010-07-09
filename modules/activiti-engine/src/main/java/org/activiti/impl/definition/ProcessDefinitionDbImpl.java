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
package org.activiti.impl.definition;

import org.activiti.impl.db.execution.DbExecutionImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.variable.DefaultVariableTypes;
import org.activiti.impl.variable.VariableTypes;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionDbImpl extends ProcessDefinitionImpl {

  private static final long serialVersionUID = 1L;

  public ProcessDefinitionDbImpl() {
    this(new DefaultVariableTypes());
  }

  public ProcessDefinitionDbImpl(VariableTypes variableTypes) {
    super(variableTypes);
  }

  public ExecutionImpl createProcessInstance() {
    DbExecutionImpl execution = new DbExecutionImpl(this);
    return execution;
  }
  
}
