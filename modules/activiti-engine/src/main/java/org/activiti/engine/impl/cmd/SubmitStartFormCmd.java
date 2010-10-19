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

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.runtime.VariableMap;
import org.activiti.engine.runtime.ProcessInstance;


/**
 * @author Tom Baeyens
 */
public class SubmitStartFormCmd implements Command<ProcessInstance> {

  protected String processDefinitionId;
  protected Map<String, Object> properties;
  
  public SubmitStartFormCmd(String processDefinitionId, Map<String, Object> properties) {
    this.processDefinitionId = processDefinitionId;
    this.properties = properties;
  }

  public ProcessInstance execute(CommandContext commandContext) {
    RepositorySession repositorySession = commandContext.getRepositorySession();
    ProcessDefinitionEntity processDefinition = repositorySession.findDeployedProcessDefinitionById(processDefinitionId);
    if (processDefinition == null) {
      throw new ActivitiException("No process definition found for id = '" + processDefinitionId + "'");
    }
    
    ExecutionEntity processInstance = null;
    StartFormHandler startFormHandler = processDefinition.getStartFormHandler();
    try {
      VariableMap.setExternalUpdate(Boolean.TRUE);

      processInstance = startFormHandler.submitStartForm(processDefinition, properties);

    } finally {
      VariableMap.setExternalUpdate(null);
    }

    processInstance.start();
    
    return processInstance;
  }
}
