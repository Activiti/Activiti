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
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.repository.DeploymentEntity;
import org.activiti.engine.impl.persistence.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.repository.ResourceEntity;
import org.activiti.engine.impl.persistence.task.TaskEntity;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.impl.definition.FormReference;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.persistence.RuntimeSession;
import org.activiti.pvm.runtime.PvmActivityInstance;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class GetFormCmd implements Command<Object> {

  private final String processDefinitionId;
  private final String processDefinitionKey;
  private final String taskId;
  
  public GetFormCmd(String processDefinitionId, String processDefinitionKey, String taskId) {
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
    this.taskId = taskId;
  }

  public Object execute(CommandContext commandContext) {
    RuntimeSession runtimeSession = commandContext.getPersistenceSession();
    RepositorySession repositorySession = commandContext.getRepositorySession();
    ProcessDefinitionEntity processDefinition = null;
    TaskEntity task = null;
    PvmActivityInstance activityInstance = null;
    FormReference formReference = null;
    
    if (taskId!=null) {
      
      task = runtimeSession.findTask(taskId);
      if (task == null) {
        throw new ActivitiException("No task found for id = '" + taskId + "'");
      }
      activityInstance = task.getActivityInstance();
      processDefinition = repositorySession.findDeployedProcessDefinitionById(task.getProcessDefinitionId());
      formReference = (FormReference) activityInstance.getActivity().getProperty(BpmnParse.PROPERTYNAME_FORM_REFERENCE);
      
    } else if (processDefinitionId!=null) {
      
      processDefinition = repositorySession.findDeployedProcessDefinitionById(processDefinitionId);
      if (processDefinition == null) {
        throw new ActivitiException("No process definition found for id = '" + processDefinitionId + "'");
      }
      formReference = (FormReference) processDefinition.getInitial().getProperty(BpmnParse.PROPERTYNAME_FORM_REFERENCE);
      
    } else if (processDefinitionKey!=null) {
      
      processDefinition = repositorySession.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
      if (processDefinition == null) {
        throw new ActivitiException("No process definition found for key '" + processDefinitionKey +"'");
      }
      formReference = (FormReference) processDefinition.getInitial().getProperty(BpmnParse.PROPERTYNAME_FORM_REFERENCE);
    } 

    String deploymentId = processDefinition.getDeploymentId();
    DeploymentEntity deployment = repositorySession.findDeploymentById(deploymentId);
    
    Object result = null;
    if (formReference != null) {
      String formLanguage = formReference.getLanguage();
      String form = formReference.getForm();
      String formTemplateString = getFormTemplateString(form, deployment);      
      
      ScriptingEngines scriptingEngines = commandContext.getProcessEngineConfiguration().getScriptingEngines();
      result = scriptingEngines.evaluate(formTemplateString, formLanguage, activityInstance);
    }

    return result;
  }

  protected String getFormTemplateString(String formResourceName, DeploymentEntity deployment) {
    // get the template
    ResourceEntity formResource = deployment.getResource(formResourceName);
    if (formResource==null) {
      throw new ActivitiException("form '"+formResource+"' not available in "+deployment);
    }
    byte[] formResourceBytes = formResource.getBytes();
    return new String(formResourceBytes);
  }
}
