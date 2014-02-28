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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.runtime.ProcessInstance;


/**
 * {@link Command} that changes the business key of an existing
 * process instance.
 * 
 * @author Tijs Rademakers
 */
public class SetProcessInstanceBusinessKeyCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  private final String processInstanceId;
  private final String businessKey;

  public SetProcessInstanceBusinessKeyCmd(String processInstanceId, String businessKey) {
    if (processInstanceId == null || processInstanceId.length() < 1) {
      throw new ActivitiIllegalArgumentException("The process instance id is mandatory, but '" + processInstanceId + "' has been provided.");
    }
    if (businessKey == null) {
      throw new ActivitiIllegalArgumentException("The business key is mandatory, but 'null' has been provided.");
    }
    
    this.processInstanceId = processInstanceId;
    this.businessKey = businessKey;
  }

  public Void execute(CommandContext commandContext) {
    ExecutionEntityManager executionManager = commandContext.getExecutionEntityManager();
    ExecutionEntity processInstance = executionManager.findExecutionById(processInstanceId);
    if (processInstance == null) {
      throw new ActivitiObjectNotFoundException("No process instance found for id = '" + processInstanceId + "'.", ProcessInstance.class);
    } else if (!processInstance.isProcessInstanceType()) {
      throw new ActivitiIllegalArgumentException(
        "A process instance id is required, but the provided id " +
        "'"+processInstanceId+"' " +
        "points to a child execution of process instance " +
        "'"+processInstance.getProcessInstanceId()+"'. " +
        "Please invoke the "+getClass().getSimpleName()+" with a root execution id.");
    }
    
    processInstance.updateProcessBusinessKey(businessKey);
    
    return null;
  }
}
