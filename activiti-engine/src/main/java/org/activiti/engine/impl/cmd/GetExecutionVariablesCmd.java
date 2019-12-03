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
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;

/**


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

    // Verify existance of execution
    if (executionId == null) {
      throw new ActivitiIllegalArgumentException("executionId is null");
    }

    ExecutionEntity execution = commandContext.getExecutionEntityManager().findById(executionId);

    if (execution == null) {
      throw new ActivitiObjectNotFoundException("execution " + executionId + " doesn't exist", Execution.class);
    }
    
    if (variableNames == null || variableNames.isEmpty()) {

      // Fetch all

      if (isLocal) {
        return execution.getVariablesLocal();
      } else {
        return execution.getVariables();
      }

    } else {

      // Fetch specific collection of variables
      if (isLocal) {
        return execution.getVariablesLocal(variableNames, false);
      } else {
        return execution.getVariables(variableNames, false);
      }

    }

  }
}
