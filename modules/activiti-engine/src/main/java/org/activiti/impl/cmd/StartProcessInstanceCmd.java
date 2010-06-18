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

import java.util.Map;

import org.activiti.ActivitiException;
import org.activiti.Configuration;
import org.activiti.ProcessInstance;
import org.activiti.impl.Cmd;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.repository.ProcessCache;
import org.activiti.impl.tx.TransactionContext;


/**
 * @author Tom Baeyens
 */
public class StartProcessInstanceCmd<T> implements Cmd<ProcessInstance> {

  String processDefinitionKey;
  String processDefinitionId;
  Map<String, Object> variables;
  
  public StartProcessInstanceCmd(String processDefinitionKey, String processDefinitionId, Map<String, Object> variables) {
    this.processDefinitionKey = processDefinitionKey;
    this.processDefinitionId = processDefinitionId;
    this.variables = variables;
  }
  
  public ProcessInstance execute(TransactionContext transactionContext) {
    ProcessCache processCache = transactionContext.getProcessEngine().getConfigurationObject(Configuration.NAME_PROCESSCACHE,ProcessCache.class);
    ProcessDefinitionImpl processDefinition = null;
    if (processDefinitionId!=null) {
      processDefinition = processCache.findProcessDefinitionById(processDefinitionId);
      if (processDefinition == null) {
        throw new ActivitiException("No process definition found for id = '" + processDefinitionId + "'");
      }
    } else {
      processDefinition = processCache.findProcessDefinitionByKey(processDefinitionKey);
      if (processDefinition == null) {
        throw new ActivitiException("No process definition found for key '" + processDefinitionKey +"'");
      }
    }
    
    ExecutionImpl processInstance = processDefinition.createProcessInstance();
    
    if (variables!=null) {
      processInstance.setVariables(variables);
    }
    
    processInstance.start();
    
    return processInstance;
  }
}
