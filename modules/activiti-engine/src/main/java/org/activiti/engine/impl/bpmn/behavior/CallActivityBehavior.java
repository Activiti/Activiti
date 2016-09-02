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
import java.util.Map;

import org.activiti.bpmn.model.MapExceptionEntry;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.data.AbstractDataAssociation;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior;


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
  private Expression processDefinitionExpression;
  protected List<MapExceptionEntry> mapExceptions;
  protected boolean inheritVariables;

  public CallActivityBehavior(String processDefinitionKey, List<MapExceptionEntry> mapExceptions) {
    this.processDefinitonKey = processDefinitionKey;
    this.mapExceptions = mapExceptions;
  }
  
  public CallActivityBehavior(Expression processDefinitionExpression, List<MapExceptionEntry> mapExceptions) {
    super();
    this.processDefinitionExpression = processDefinitionExpression;
    this.mapExceptions = mapExceptions;
  }

  public void addDataInputAssociation(AbstractDataAssociation dataInputAssociation) {
    this.dataInputAssociations.add(dataInputAssociation);
  }

  public void addDataOutputAssociation(AbstractDataAssociation dataOutputAssociation) {
    this.dataOutputAssociations.add(dataOutputAssociation);
  }

  public void setInheritVariables(boolean inheritVariables) {
    this.inheritVariables = inheritVariables;
  }

  public void execute(ActivityExecution execution) throws Exception {
    
    String processDefinitonKey = this.processDefinitonKey;
    if (processDefinitionExpression != null) {
      processDefinitonKey = (String) processDefinitionExpression.getValue(execution);
    }
    
    DeploymentManager deploymentManager = Context.getProcessEngineConfiguration().getDeploymentManager();

    ProcessDefinitionEntity processDefinition = null;
    if (execution.getTenantId() == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(execution.getTenantId())) {
    	processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKey(processDefinitonKey);
    } else {
    	processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKeyAndTenantId(processDefinitonKey, execution.getTenantId());
    }

    // Do not start a process instance if the process definition is suspended
    if (deploymentManager.isProcessDefinitionSuspended(processDefinition.getId())) {
      throw new ActivitiException("Cannot start process instance. Process definition "
          + processDefinition.getName() + " (id = " + processDefinition.getId() + ") is suspended");
    }
    
    PvmProcessInstance subProcessInstance = execution.createSubProcessInstance(processDefinition);

    if (inheritVariables) {
      Map<String, Object> variables = execution.getVariables();
      for (Map.Entry<String, Object> entry : variables.entrySet()) {
        subProcessInstance.setVariable(entry.getKey(), entry.getValue());
      }
    }
    
    // copy process variables
    for (AbstractDataAssociation dataInputAssociation : dataInputAssociations) {
      Object value = null;
      if (dataInputAssociation.getSourceExpression()!=null) {
        value = dataInputAssociation.getSourceExpression().getValue(execution);
      }
      else {
        value = execution.getVariable(dataInputAssociation.getSource());
      }
      subProcessInstance.setVariable(dataInputAssociation.getTarget(), value);
    }
    
    try {
      subProcessInstance.start();
    } catch (Exception e) {
        if (!ErrorPropagation.mapException(e, execution, mapExceptions, true))
            throw e;
        
      }
      
  }
  
  public void setProcessDefinitonKey(String processDefinitonKey) {
    this.processDefinitonKey = processDefinitonKey;
  }
  
  public String getProcessDefinitonKey() {
    return processDefinitonKey;
  }
  
  public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {
    // only data.  no control flow available on this execution.

    // copy process variables
    for (AbstractDataAssociation dataOutputAssociation : dataOutputAssociations) {
      Object value = null;
      if (dataOutputAssociation.getSourceExpression()!=null) {
        value = dataOutputAssociation.getSourceExpression().getValue(subProcessInstance);
      }
      else {
        value = subProcessInstance.getVariable(dataOutputAssociation.getSource());
      }
      
      execution.setVariable(dataOutputAssociation.getTarget(), value);
    }
  }

  public void completed(ActivityExecution execution) throws Exception {
    // only control flow.  no sub process instance data available
    leave(execution);
  }

}
