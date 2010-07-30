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

package org.activiti.engine.impl.bpmn;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.RepositorySession;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.ObjectProcessInstance;


/**
 * Implementation of the BPMN 2.0 call activity
 * (limited currently to calling a subprocess and not (yet) a global task).
 * 
 * @author Joram Barrez
 */
public class CallActivityBehaviour extends BpmnActivity {
  
  protected String processDefinitonKey;
  
  public CallActivityBehaviour(String processDefinitionKey) {
    this.processDefinitonKey = processDefinitionKey;
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    RepositorySession repositorySession = CommandContext.getCurrent().getRepositorySession();
    ProcessDefinitionImpl processDefinition =  repositorySession.findDeployedLatestProcessDefinitionByKey(processDefinitonKey);
    
    ObjectProcessInstance processInstance = execution.createSubProcessInstance(processDefinition);
    processInstance.start();
  }
  
  public void event(ActivityExecution execution, Object event) throws Exception {
    leave(execution);
  }

}
