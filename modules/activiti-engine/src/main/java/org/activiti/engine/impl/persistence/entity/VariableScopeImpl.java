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
package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;



/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public abstract class VariableScopeImpl implements Serializable, VariableScope {
  
  private static final long serialVersionUID = 1L;
  
  protected Map<String, VariableInstanceEntity> variableInstances = null;
  protected List<VariableInstanceEntity> variableInstanceList = null;
  
  protected ELContext cachedElContext;

  protected String id = null;

  protected abstract List<VariableInstanceEntity> loadVariableInstances();
  protected abstract VariableScopeImpl getParentVariableScope();
  protected abstract void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance);

  protected void ensureVariableInstancesInitialized() {
    if (variableInstances==null) {
      variableInstances = new HashMap<String, VariableInstanceEntity>();
      variableInstanceList = new ArrayList<VariableInstanceEntity>();
      
      CommandContext commandContext = Context.getCommandContext();
      if (commandContext == null) {
        throw new ActivitiException("lazy loading outside command context");
      }
      List<VariableInstanceEntity> variableInstancesList = loadVariableInstances();
      for (VariableInstanceEntity variableInstance : variableInstancesList) {
        variableInstances.put(variableInstance.getName(), variableInstance);
        variableInstanceList.add(variableInstance);
      }
    }
  }
  
  public Map<String, Object> getVariables() {
    return collectVariables(new HashMap<String, Object>());
  }
  
  protected Map<String, Object> collectVariables(HashMap<String, Object> variables) {
    ensureVariableInstancesInitialized();
    VariableScopeImpl parentScope = getParentVariableScope();
    if (parentScope!=null) {
      variables.putAll(parentScope.collectVariables(variables));
    }
    for (VariableInstanceEntity variableInstance: variableInstances.values()) {
      variables.put(variableInstance.getName(), variableInstance.getValue());
    }
    return variables;
  }
  
  public Object getVariable(String variableName) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(variableName);
    if (variableInstance!=null) {
      return variableInstance.getValue();
    }
    VariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.getVariable(variableName);
    }
    return null;
  }
  
  public Object getVariableLocal(String variableName) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(variableName);
    if (variableInstance!=null) {
      return variableInstance.getValue();
    }
    return null;
  }
  
  public boolean hasVariables() {
    ensureVariableInstancesInitialized();
    if (!variableInstances.isEmpty()) {
      return true;
    }
    VariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.hasVariables();
    }
    return false;
  }

  public boolean hasVariablesLocal() {
    ensureVariableInstancesInitialized();
    return !variableInstances.isEmpty();
  }

  public boolean hasVariable(String variableName) {
    if (hasVariableLocal(variableName)) {
      return true;
    }
    VariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.hasVariable(variableName);
    }
    return false;
  }

  public boolean hasVariableLocal(String variableName) {
    ensureVariableInstancesInitialized();
    return variableInstances.containsKey(variableName);
  }

  protected Set<String> collectVariableNames(Set<String> variableNames) {
    ensureVariableInstancesInitialized();
    VariableScopeImpl parentScope = getParentVariableScope();
    if (parentScope!=null) {
      variableNames.addAll(parentScope.collectVariableNames(variableNames));
    }
    for (VariableInstanceEntity variableInstance: variableInstances.values()) {
      variableNames.add(variableInstance.getName());
    }
    return variableNames;
  }

  public Set<String> getVariableNames() {
    return collectVariableNames(new HashSet<String>());
  }
  
  public Map<String, Object> getVariablesLocal() {
    Map<String, Object> variables = new HashMap<String, Object>();
    ensureVariableInstancesInitialized();
    for (VariableInstanceEntity variableInstance: variableInstances.values()) {
      variables.put(variableInstance.getName(), variableInstance.getValue());
    }
    return variables;
  }

  public Set<String> getVariableNamesLocal() {
    ensureVariableInstancesInitialized();
    return variableInstances.keySet();
  }

  public void createVariablesLocal(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (Map.Entry<String, ? extends Object> entry: variables.entrySet()) {
        createVariableLocal(entry.getKey(), entry.getValue());
      }
    }
  }

  public void setVariables(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (String variableName : variables.keySet()) {
        setVariable(variableName, variables.get(variableName));
      }
    }
  }
  
  public void setVariablesLocal(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (String variableName : variables.keySet()) {
        setVariableLocal(variableName, variables.get(variableName));
      }
    }
  }

  public void removeVariables() {
    ensureVariableInstancesInitialized();
    Set<String> variableNames = new HashSet<String>(variableInstances.keySet());
    for (String variableName: variableNames) {
      removeVariable(variableName);
    }
  }
  
  public void removeVariablesLocal() {
    List<String> variableNames = new ArrayList<String>(getVariableNamesLocal());
    for (String variableName: variableNames) {
      removeVariableLocal(variableName);
    }
  }
  
  public void deleteVariablesInstanceForLeavingScope() {
    ensureVariableInstancesInitialized();
    
    for (VariableInstanceEntity variableInstance: variableInstanceList) {
        Context.getCommandContext().getHistoryManager()
          .recordVariableUpdate(variableInstance);
        
        variableInstance.delete();
    }
    variableInstanceList.clear();
  }
  
  public void removeVariables(Collection<String> variableNames) {
    if (variableNames != null) {
      for (String variableName : variableNames) {
        removeVariable(variableName);
      }
    }
  }

  public void removeVariablesLocal(Collection<String> variableNames) {
    if (variableNames != null) {
      for (String variableName : variableNames) {
        removeVariableLocal(variableName);
      }
    }
  }

  public void setVariable(String variableName, Object value) {
    setVariable(variableName, value, getSourceActivityExecution());
  }

  protected void setVariable(String variableName, Object value, ExecutionEntity sourceActivityExecution) {
    if (hasVariableLocal(variableName)) {
      setVariableLocal(variableName, value, sourceActivityExecution);
      return;
    } 
    VariableScopeImpl parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      if (sourceActivityExecution==null) {
        parentVariableScope.setVariable(variableName, value);
      } else {
        parentVariableScope.setVariable(variableName, value, sourceActivityExecution);
      }
      return;
    }
    createVariableLocal(variableName, value);
  }

  public Object setVariableLocal(String variableName, Object value) {
    return setVariableLocal(variableName, value, getSourceActivityExecution());
  }

  public Object setVariableLocal(String variableName, Object value, ExecutionEntity sourceActivityExecution) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(variableName);
    if (variableInstance == null) {
      createVariableLocal(variableName, value);
    } else {
      updateVariableInstance(variableInstance, value, sourceActivityExecution);
    }
    
    return null;
  }
  
  public void createVariableLocal(String variableName, Object value) {
    createVariableLocal(variableName, value, getSourceActivityExecution());
  }

  /** only called when a new variable is created on this variable scope.
   * This method is also responsible for propagating the creation of this 
   * variable to the history. */
  protected void createVariableLocal(String variableName, Object value, ExecutionEntity sourceActivityExecution) {
    ensureVariableInstancesInitialized();
    
    if (variableInstances.containsKey(variableName)) {
      throw new ActivitiException("variable '"+variableName+"' already exists. Use setVariableLocal if you want to overwrite the value");
    }
    
    createVariableInstance(variableName, value, sourceActivityExecution);
  }

  public void removeVariable(String variableName) {
    removeVariable(variableName, getSourceActivityExecution());
  }

  protected void removeVariable(String variableName, ExecutionEntity sourceActivityExecution) {
    ensureVariableInstancesInitialized();
    if (variableInstances.containsKey(variableName)) {
      removeVariableLocal(variableName);
      return;
    }
    VariableScopeImpl parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      if (sourceActivityExecution==null) {
        parentVariableScope.removeVariable(variableName);
      } else {
        parentVariableScope.removeVariable(variableName, sourceActivityExecution);
      }
    }
  }

  public void removeVariableLocal(String variableName) {
    removeVariableLocal(variableName, getSourceActivityExecution());
  }

  protected ExecutionEntity getSourceActivityExecution() {
    return null;
  }
  
  protected void removeVariableLocal(String variableName, ExecutionEntity sourceActivityExecution) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.remove(variableName);
    if (variableInstance != null) {
      deleteVariableInstanceForExplicitUserCall(variableInstance, sourceActivityExecution);
      variableInstanceList.remove(variableInstance);
    }
  }

  protected void deleteVariableInstanceForExplicitUserCall(VariableInstanceEntity variableInstance, ExecutionEntity sourceActivityExecution) {
    variableInstance.delete();
    variableInstance.setValue(null);

    // Record historic variable
    Context.getCommandContext().getHistoryManager()
      .recordVariableUpdate(variableInstance);

    // Record historic detail
    Context.getCommandContext().getHistoryManager()
      .recordHistoricDetailVariableCreate(variableInstance, sourceActivityExecution,  isActivityIdUsedForDetails());
  }

  protected void updateVariableInstance(VariableInstanceEntity variableInstance, Object value, ExecutionEntity sourceActivityExecution) {
	
      // type should be changed
	 if ((variableInstance != null) && (!variableInstance.getType().isAbleToStore(value))) {
		    VariableTypes variableTypes = Context
		    	      .getProcessEngineConfiguration()
		    	      .getVariableTypes();
		    VariableType newType = variableTypes.findVariableType(value);
		    variableInstance.setValue(null);
		    variableInstance.setType(newType);
		    variableInstance.forceUpdate();
		    variableInstance.setValue(value);
		    VariableInstanceEntity.touch(variableInstance);
	  } else
		  variableInstance.setValue(value);

    Context.getCommandContext().getHistoryManager()
      .recordHistoricDetailVariableCreate(variableInstance, sourceActivityExecution, isActivityIdUsedForDetails());
    
    Context.getCommandContext().getHistoryManager()
      .recordVariableUpdate(variableInstance);
  }

  protected VariableInstanceEntity createVariableInstance(String variableName, Object value, ExecutionEntity sourceActivityExecution) {
    VariableTypes variableTypes = Context
      .getProcessEngineConfiguration()
      .getVariableTypes();
    
    VariableType type = variableTypes.findVariableType(value);
 
    VariableInstanceEntity variableInstance = VariableInstanceEntity.createAndInsert(variableName, type, value);
    initializeVariableInstanceBackPointer(variableInstance);
    variableInstances.put(variableName, variableInstance);
    variableInstanceList.add(variableInstance);
    
    // Record historic variable
    Context.getCommandContext().getHistoryManager()
      .recordVariableCreate(variableInstance);

    // Record historic detail
    Context.getCommandContext().getHistoryManager()
      .recordHistoricDetailVariableCreate(variableInstance, sourceActivityExecution, isActivityIdUsedForDetails());

    return variableInstance;
  }

  
  /** 
   * Execution variable updates have activity instance ids, but historic task variable updates don't.
   */
  protected boolean isActivityIdUsedForDetails() {
    return true;
  }

  // getters and setters //////////////////////////////////////////////////////

  public ELContext getCachedElContext() {
    return cachedElContext;
  }
  public void setCachedElContext(ELContext cachedElContext) {
    this.cachedElContext = cachedElContext;
  }
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
}
