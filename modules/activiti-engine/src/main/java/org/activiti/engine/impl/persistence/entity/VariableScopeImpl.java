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
import java.util.Collection;
import java.util.Collections;
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
 * @author Saeid Mirzaei
 */
public abstract class VariableScopeImpl implements Serializable, VariableScope {
  
  private static final long serialVersionUID = 1L;
  
  protected Map<String, VariableInstanceEntity> variableInstances = null;
  
  
  protected ELContext cachedElContext;

  protected String id = null;

  protected abstract List<VariableInstanceEntity> loadVariableInstances();
  protected abstract VariableInstanceEntity loadVariableInstance(String variableName);
  protected abstract Long getVariablesCount();
  protected abstract VariableScopeImpl getParentVariableScope();
  protected abstract void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance);

  boolean isLazyLoadingSet() {
    return Context.getProcessEngineConfiguration().isLazyLoadVariables();
  }
  
  protected void loadAllVariables() {
      CommandContext commandContext = Context.getCommandContext();
      if (commandContext == null) {
        throw new ActivitiException("lazy loading outside command context");
      }
      if (variableInstances==null) 
        variableInstances = new HashMap<String, VariableInstanceEntity>();
      
      List<VariableInstanceEntity> variableInstancesList = loadVariableInstances();
      for (VariableInstanceEntity variableInstance : variableInstancesList) {
        variableInstances.put(variableInstance.getName(), variableInstance);      
      }
      
  }
  
  // initialized the variableInstance, only load it if not lazy
  protected void ensureVariableInstancesInitialized() {
    if (variableInstances==null) {
      variableInstances = new HashMap<String, VariableInstanceEntity>();
      if (!isLazyLoadingSet()) 
        loadAllVariables();
    }
  }
  
 //initialized the variableInstance, loaded
  protected void ensureVariableInstancesLoaded() {
    if (variableInstances == null || isLazyLoadingSet() ) 
       loadAllVariables();
  }
 
  public Map<String, Object> getVariables() {
    VariableScopeImpl parentScope = getParentVariableScope();
    Map<String, Object> variables = parentScope != null ? parentScope.getVariables() : new HashMap<String, Object>();
    loadAllVariables();

    for (VariableInstanceEntity variableInstance: variableInstances.values()) {
      variables.put(variableInstance.getName(), variableInstance.getValue());
    }
    return variables;
  }
  
  public Object getVariable(String variableName) {
    Object result = getVariableLocal(variableName);
    if (result != null)
      return result;
    
    VariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.getVariable(variableName);
    }
    return null;
  }
  
  public Object getVariableLocal(String variableName) {
    VariableInstanceEntity variableInstanceEntity = getVariableLocalInstanceEntity(variableName);
    return variableInstanceEntity == null ? null : variableInstanceEntity.getValue();
    
  }
    
  protected VariableInstanceEntity getVariableLocalInstanceEntity(String variableName) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(variableName);
    if (variableInstance!=null) {
      return variableInstance;
    }
    
    if (isLazyLoadingSet()) {
      variableInstance = loadVariableInstance(variableName);
      if (variableInstance != null) {
        variableInstances.put(variableInstance.getName(), variableInstance);
        return variableInstance;
      }
    }
    return null;
  }
  
  public boolean hasVariables() {
    ensureVariableInstancesInitialized();
    if (!variableInstances.isEmpty()) {
      return true;
    }
    VariableScope parentScope = getParentVariableScope();
    if (parentScope!=null && parentScope.hasVariables()) {
      return true;
    }
    
    return isLazyLoadingSet() && getVariablesCount() > 0;
  }

  public boolean hasVariablesLocal() {
    ensureVariableInstancesInitialized();
    return !variableInstances.isEmpty() || (isLazyLoadingSet() && getVariablesCount() > 0) ;
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
    return variableInstances.containsKey(variableName) 
            || (isLazyLoadingSet() &&  loadVariableInstance(variableName) != null);
  }

  public Set<String> getVariableNames() {
    VariableScopeImpl parentScope = getParentVariableScope();
    Set<String> variableNames = parentScope != null ?  parentScope.getVariableNames() : new HashSet<String>();
    ensureVariableInstancesLoaded();
    variableNames.addAll(variableInstances.keySet());
    return variableNames;
  }

  
  public Map<String, Object> getVariablesLocal() {
    Map<String, Object> variables = new HashMap<String, Object>();
    ensureVariableInstancesLoaded();
    
    for (VariableInstanceEntity variableInstance: variableInstances.values()) {
      variables.put(variableInstance.getName(), variableInstance.getValue());
    }
    return variables;
  }

  public Set<String> getVariableNamesLocal() {
    ensureVariableInstancesLoaded();
    return variableInstances.keySet();
  }

  public Map<String, VariableInstanceEntity> getVariableInstances()
  {
    ensureVariableInstancesLoaded();
    return Collections.unmodifiableMap(variableInstances);
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
    for (String variableName: getVariableNames()) {
      removeVariable(variableName);
    }    
  }
  
  public void removeVariablesLocal() {
    for (String variableName: getVariableNamesLocal()) {
      removeVariableLocal(variableName);
    }
  }
  
  public void deleteVariablesInstanceForLeavingScope() {
    ensureVariableInstancesLoaded();
    
    for (VariableInstanceEntity variableInstance: variableInstances.values()) {
        Context.getCommandContext().getHistoryManager()
          .recordVariableUpdate(variableInstance);
        
        variableInstance.delete();
    }
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
    VariableInstanceEntity variableInstance = getVariableLocalInstanceEntity(variableName);
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
    VariableInstanceEntity variableInstance = getVariableLocalInstanceEntity(variableName);
    
    if (variableInstance  != null) {
      throw new ActivitiException("variable '"+variableName+"' already exists. Use setVariableLocal if you want to overwrite the value");
    }
    
    createVariableInstance(variableName, value, sourceActivityExecution);
  }

  public void removeVariable(String variableName) {
    removeVariable(variableName, getSourceActivityExecution());
  }

  protected void removeVariable(String variableName, ExecutionEntity sourceActivityExecution) {
    VariableInstanceEntity variableInstance = getVariableLocalInstanceEntity(variableName);
    if (variableInstance != null) {
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
    VariableInstanceEntity variableInstance = getVariableLocalInstanceEntity(variableName);
    if (variableInstance != null) {
       variableInstance = variableInstances.remove(variableName);
       variableInstance.delete();
       variableInstance.setValue(null);

       // Record historic variable
       Context.getCommandContext().getHistoryManager()
         .recordVariableUpdate(variableInstance);

       // Record historic detail
       Context.getCommandContext().getHistoryManager()
         .recordHistoricDetailVariableCreate(variableInstance, sourceActivityExecution,  isActivityIdUsedForDetails());
    }
  }

  protected void updateVariableInstance(VariableInstanceEntity variableInstance, Object value, ExecutionEntity sourceActivityExecution) {
	
    // Always check if the type should be altered. It's possible that the previous type is lower in the type
    // checking chain (eg. serializable) and will return true on isAbleToStore(), even though another type
    // higher in the chain is eligable for storage.
    
    VariableTypes variableTypes = Context
        .getProcessEngineConfiguration()
        .getVariableTypes();
    
    VariableType newType = variableTypes.findVariableType(value);
    
	 if ((variableInstance != null) && (!variableInstance.getType().equals(newType))) {
		variableInstance.setValue(null);
		variableInstance.setType(newType);
		variableInstance.forceUpdate();
		variableInstance.setValue(value);
		VariableInstanceEntity.touch(variableInstance);
	  } else {
	    variableInstance.setValue(value);
	  }

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

    public <T> T getVariable(String variableName, Class<T> variableClass){
        return variableClass.cast(getVariable(variableName));
    }

    public <T> T getVariableLocal(String variableName, Class<T> variableClass){
        return variableClass.cast(getVariableLocal(variableName));
    }
}
