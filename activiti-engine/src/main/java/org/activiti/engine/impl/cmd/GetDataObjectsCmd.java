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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.impl.DataObjectImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.util.Activiti5Util;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.runtime.DataObject;
import org.activiti.engine.runtime.Execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class GetDataObjectsCmd implements Command<Map<String, DataObject>>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String executionId;
  protected Collection<String> dataObjectNames;
  protected boolean isLocal;
  protected String locale;
  protected boolean withLocalizationFallback;
  
  public GetDataObjectsCmd(String executionId, Collection<String> dataObjectNames, boolean isLocal) {
    this.executionId = executionId;
    this.dataObjectNames = dataObjectNames;
    this.isLocal = isLocal;
  }

  public GetDataObjectsCmd(String executionId, Collection<String> dataObjectNames, boolean isLocal, String locale, boolean withLocalizationFallback) {
    this.executionId = executionId;
    this.dataObjectNames = dataObjectNames;
    this.isLocal = isLocal;
    this.locale = locale;
    this.withLocalizationFallback = withLocalizationFallback;
  }

  public Map<String, DataObject> execute(CommandContext commandContext) {

    // Verify existance of execution
    if (executionId == null) {
      throw new ActivitiIllegalArgumentException("executionId is null");
    }

    ExecutionEntity execution = commandContext.getExecutionEntityManager().findById(executionId);

    if (execution == null) {
      throw new ActivitiObjectNotFoundException("execution " + executionId + " doesn't exist", Execution.class);
    }
    
    Map<String, VariableInstance> variables = null;
    
    if (Activiti5Util.isActiviti5ProcessDefinitionId(commandContext, execution.getProcessDefinitionId())) {
      Activiti5CompatibilityHandler activiti5CompatibilityHandler = Activiti5Util.getActiviti5CompatibilityHandler(); 
      variables = activiti5CompatibilityHandler.getExecutionVariableInstances(executionId, dataObjectNames, isLocal);
    
    } else {

      if (dataObjectNames == null || dataObjectNames.isEmpty()) {
        // Fetch all
        if (isLocal) {
          variables = execution.getVariableInstancesLocal();
        } else {
          variables = execution.getVariableInstances();
        }
  
      } else {
        // Fetch specific collection of variables
        if (isLocal) {
          variables = execution.getVariableInstancesLocal(dataObjectNames, false);
        } else {
          variables = execution.getVariableInstances(dataObjectNames, false);
        }
      }
    }

    Map<String,DataObject> dataObjects = null;
    if (variables != null) {
      dataObjects = new HashMap<>(variables.size());
      
      for (Entry<String, VariableInstance> entry : variables.entrySet()) {
        String name = entry.getKey();
        VariableInstance variableEntity = (VariableInstance) entry.getValue();

        ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findById(variableEntity.getExecutionId());
        while (!executionEntity.isScope()) {
          executionEntity = executionEntity.getParent();
        }
        
        BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(execution.getProcessDefinitionId());
        ValuedDataObject foundDataObject = null;
        if (executionEntity.getParentId() == null) {
          for (ValuedDataObject dataObject : bpmnModel.getMainProcess().getDataObjects()) {
            if (dataObject.getName().equals(variableEntity.getName())) {
              foundDataObject = dataObject;
              break;
            }
          }
        } else {
          SubProcess subProcess = (SubProcess) bpmnModel.getFlowElement(execution.getActivityId());
          for (ValuedDataObject dataObject : subProcess.getDataObjects()) {
            if (dataObject.getName().equals(variableEntity.getName())) {
              foundDataObject = dataObject;
              break;
            }
          }
        }
        
        String localizedName = null;
        String localizedDescription = null;
        
        if (locale != null && foundDataObject != null) {          
          ObjectNode languageNode = Context.getLocalizationElementProperties(locale, foundDataObject.getId(), 
              execution.getProcessDefinitionId(), withLocalizationFallback);
          
          if (languageNode != null) {
            JsonNode nameNode = languageNode.get(DynamicBpmnConstants.LOCALIZATION_NAME);
            if (nameNode != null) {
              localizedName = nameNode.asText();
            }
            JsonNode descriptionNode = languageNode.get(DynamicBpmnConstants.LOCALIZATION_DESCRIPTION);
            if (descriptionNode != null) {
              localizedDescription = descriptionNode.asText();
            }
          }
        }
        
        if (foundDataObject != null) {
          dataObjects.put(name, new DataObjectImpl(variableEntity.getName(), variableEntity.getValue(), foundDataObject.getDocumentation(), 
              foundDataObject.getType(), localizedName, localizedDescription, foundDataObject.getId()));
        }
      }
    }
    
    return dataObjects;
  }
}
