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
package org.activiti.impl.cmd;

import org.activiti.ActivitiException;
import org.activiti.impl.bytes.ByteArrayImpl;
import org.activiti.impl.definition.FormReference;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.repository.DeploymentImpl;
import org.activiti.impl.repository.ProcessCache;
import org.activiti.impl.scripting.ScriptingEngines;
import org.activiti.impl.task.TaskImpl;


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
    ProcessCache processCache = commandContext.getProcessCache();
    ProcessDefinitionImpl processDefinition = null;
    TaskImpl task = null;
    ExecutionImpl execution = null;
    FormReference formReference = null;
    
    if (taskId!=null) {
      
      PersistenceSession persistenceSession = commandContext.getPersistenceSession();
      task = persistenceSession.findTask(taskId);
      if (task == null) {
        throw new ActivitiException("No task found for id = '" + taskId + "'");
      }
      execution = task.getExecution();
      processDefinition = processCache.findProcessDefinitionById(task.getProcessDefinitionId());
      formReference = execution.getActivity().getFormReference();
      
    } else if (processDefinitionId!=null) {
      
      processDefinition = processCache.findProcessDefinitionById(processDefinitionId);
      if (processDefinition == null) {
        throw new ActivitiException("No process definition found for id = '" + processDefinitionId + "'");
      }
      formReference = processDefinition.getInitial().getFormReference();
      
    } else if (processDefinitionKey!=null) {
      
      processDefinition = processCache.findProcessDefinitionByKey(processDefinitionKey);
      if (processDefinition == null) {
        throw new ActivitiException("No process definition found for key '" + processDefinitionKey +"'");
      }
      formReference = processDefinition.getInitial().getFormReference();
    } 

    DeploymentImpl deployment = processDefinition.getDeployment();
    
    Object result = null;
    if (formReference != null) {
      String formLanguage = formReference.getLanguage();
      String form = formReference.getForm();
      String formTemplateString = getFormTemplateString(form, deployment);      
      
      ScriptingEngines scriptingEngines = commandContext.getScriptingEngines();
      result = scriptingEngines.evaluate(formTemplateString, formLanguage, execution);
    }

    return result;
  }

  protected String getFormTemplateString(String formResource, DeploymentImpl deployment) {
    // get the template
    ByteArrayImpl formResourceByteArray = deployment.getResource(formResource);
    if (formResourceByteArray==null) {
      throw new ActivitiException("form '"+formResource+"' not available in "+deployment);
    }
    byte[] formResourceBytes = formResourceByteArray.getBytes();
    return new String(formResourceBytes);
  }
}
