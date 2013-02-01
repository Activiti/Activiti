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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;


/**
 * @author Tom Baeyens
 */
public class GetExecutionVariablesCmd implements Command<Map<String, Object>>, Serializable {

  private static final long serialVersionUID = 1L;
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
      throw new ActivitiIllegalArgumentException("executionId is null");
    }
    
    ExecutionEntity execution = commandContext
      .getExecutionEntityManager()
      .findExecutionById(executionId);
    
    if (execution==null) {
      throw new ActivitiObjectNotFoundException("execution "+executionId+" doesn't exist", Execution.class);
    }

    Map<String, Object> executionVariables;
    if (isLocal) {
      executionVariables = execution.getVariablesLocal();
    } else {
      executionVariables = execution.getVariables();
    }
    
    if (variableNames != null && variableNames.size() > 0) {
      // if variableNames is not empty, return only variable names mentioned in it
      Map<String, Object> tempVariables = new HashMap<String, Object>();
      for (String variableName: variableNames) {
        if (executionVariables.containsKey(variableName)) {
          tempVariables.put(variableName, executionVariables.get(variableName));
        }
      }
      executionVariables = tempVariables;
    }
    
    return executionVariables;
  }
}
