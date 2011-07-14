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
package org.activiti.engine.impl.cmd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


/**
 * @author Tom Baeyens
 */
public class GetExecutionVariablesCmd implements Command<Map<String, Object>> {

  protected String executionId;
  protected Collection<String> variableNames;
  protected boolean isLocal;

  public GetExecutionVariablesCmd(String executionId, Collection<String> variableNames, boolean isLocal) {
    this.executionId = executionId;
    this.variableNames = variableNames;
    this.isLocal = isLocal;
  }

  public Map<String, Object> execute(CommandContext commandContext) {
    if(executionId == null) {
      throw new ActivitiException("executionId is null");
    }
    
    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(executionId);
    
    if (execution==null) {
      throw new ActivitiException("execution "+executionId+" doesn't exist");
    }

    Map<String, Object> executionVariables;
    if (isLocal) {
      executionVariables = execution.getVariablesLocal();
    } else {
      executionVariables = execution.getVariables();
    }
    
    return executionVariables;
    
    /*if (variableNames==null) {
      variableNames = executionVariables.keySet();
    }*/
    
    // this copy is made to avoid lazy initialization outside a command context
    /*Map<String, Object> variables = new HashMap<String, Object>();
    for (String variableName: variableNames) {
      variables.put(variableName, execution.getVariable(variableName));
    }
    
    return variables;*/
  }
}
