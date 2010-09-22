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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.repository.DeploymentEntity;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.repository.ResourceEntity;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.task.TaskEntity;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class GetFormCmd implements Command<Object> {

  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String taskId;
  
  public GetFormCmd(String processDefinitionId, String processDefinitionKey, String taskId) {
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
    this.taskId = taskId;
  }

  public Object execute(CommandContext commandContext) {
    RepositorySession repositorySession = commandContext.getRepositorySession();
    ProcessDefinitionEntity processDefinition = null;
    TaskEntity task = null;
    ExecutionEntity execution = null;
    String formResourceKey = null;
    
    if (taskId!=null) {
      task = commandContext
        .getTaskSession()
        .findTaskById(taskId);
      
      if (task == null) {
        throw new ActivitiException("No task found for id = '" + taskId + "'");
      }
      
      execution = task.getExecution();
      processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();
      
      formResourceKey = task.getFormResourceKey();
      
    } else if (processDefinitionId!=null) {
      
      processDefinition = repositorySession.findDeployedProcessDefinitionById(processDefinitionId);
      if (processDefinition == null) {
        throw new ActivitiException("No process definition found for id = '" + processDefinitionId + "'");
      }
      formResourceKey = processDefinition.getStartFormResourceKey();
      
      
    } else if (processDefinitionKey!=null) {
      
      processDefinition = repositorySession.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
      if (processDefinition == null) {
        throw new ActivitiException("No process definition found for key '" + processDefinitionKey +"'");
      }
      formResourceKey = processDefinition.getStartFormResourceKey();
    } else {
      throw new ActivitiException("processDefinitionKey, processDefinitionId and taskId are null");
    }

    Object result = null;
    if (formResourceKey != null) {
      String deploymentId = processDefinition.getDeploymentId();
      DeploymentEntity deployment = repositorySession.findDeploymentById(deploymentId);

      String formTemplateString = getFormTemplateString(formResourceKey, deployment);
      
      ScriptingEngines scriptingEngines = commandContext.getProcessEngineConfiguration().getScriptingEngines();
      result = scriptingEngines.evaluate(formTemplateString, ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE, execution);
    }

    return result;
  }

  protected String getFormTemplateString(String formResourceName, DeploymentEntity deployment) {
    // get the template
    ResourceEntity formResource = deployment.getResource(formResourceName);
    if (formResource==null) {
      throw new ActivitiException("form '"+formResourceName+"' not available in "+deployment);
    }
    byte[] formResourceBytes = formResource.getBytes();
    return new String(formResourceBytes);
  }
}
