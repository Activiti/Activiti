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

package org.activiti.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.data.AbstractDataAssociation;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;


/**
 * Implementation of the BPMN 2.0 call activity
 * (limited currently to calling a subprocess and not (yet) a global task).
 * 
 * @author Joram Barrez
 */
public class CallActivityBehavior extends AbstractBpmnActivityBehavior implements SubProcessActivityBehavior {
  
  protected String processDefinitonKey;
  private List<AbstractDataAssociation> dataInputAssociations = new ArrayList<AbstractDataAssociation>();
  private List<AbstractDataAssociation> dataOutputAssociations = new ArrayList<AbstractDataAssociation>();
  
  public CallActivityBehavior(String processDefinitionKey) {
    this.processDefinitonKey = processDefinitionKey;
  }

  public void addDataInputAssociation(AbstractDataAssociation dataInputAssociation) {
    this.dataInputAssociations.add(dataInputAssociation);
  }

  public void addDataOutputAssociation(AbstractDataAssociation dataOutputAssociation) {
    this.dataOutputAssociations.add(dataOutputAssociation);
  }

  public void execute(ActivityExecution execution) throws Exception {
    ProcessDefinitionImpl processDefinition = Context
      .getCommandContext()
      .getRepositorySession()
      .findDeployedLatestProcessDefinitionByKey(processDefinitonKey);
    
    PvmProcessInstance subProcessInstance = execution.createSubProcessInstance(processDefinition);
    
    // copy process variables
    for (AbstractDataAssociation dataInputAssociation : dataInputAssociations) {
      Object value = execution.getVariable(dataInputAssociation.getSource());
      subProcessInstance.setVariable(dataInputAssociation.getTarget(), value);
    }
    
    subProcessInstance.start();
  }
  
  public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {
    // only data.  no control flow available on this execution.

    // copy process variables
    for (AbstractDataAssociation dataOutputAssociation : dataOutputAssociations) {
      Object value = subProcessInstance.getVariable(dataOutputAssociation.getSource());
      execution.setVariable(dataOutputAssociation.getTarget(), value);
    }
  }

  public void completed(ActivityExecution execution) throws Exception {
    // only control flow.  no sub process instance data available
    leave(execution);
  }

}
