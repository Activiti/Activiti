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
import org.activiti.pvm.activity.ActivityExecution;
import org.activiti.pvm.activity.SubProcessActivityBehavior;
import org.activiti.pvm.delegate.DelegateExecution;
import org.activiti.pvm.impl.process.ProcessDefinitionImpl;
import org.activiti.pvm.runtime.PvmProcessInstance;


/**
 * Implementation of the BPMN 2.0 call activity
 * (limited currently to calling a subprocess and not (yet) a global task).
 * 
 * @author Joram Barrez
 */
public class CallActivityBehaviour extends AbstractBpmnActivity implements SubProcessActivityBehavior {
  
  protected String processDefinitonKey;
  
  public CallActivityBehaviour(String processDefinitionKey) {
    this.processDefinitonKey = processDefinitionKey;
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    ProcessDefinitionImpl processDefinition = 
      CommandContext
        .getCurrent()
        .getRepositorySession()
        .findDeployedLatestProcessDefinitionByKey(processDefinitonKey);
    
    PvmProcessInstance processInstance = execution.createSubProcessInstance(processDefinition);
    processInstance.start();
  }
  
  public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {
    // only data.  no control flow available on this execution.
  }

  public void completed(ActivityExecution execution) throws Exception {
    // only control flow.  no sub process instance data available
    leave(execution);
  }
}
