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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;



/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public abstract class VariableScopeImpl implements Serializable, VariableScope {
  
  private static final long serialVersionUID = 1L;
  
  protected Map<String, VariableInstanceEntity> variableInstances = null;
  
  protected ELContext cachedElContext;

  protected abstract List<VariableInstanceEntity> loadVariableInstances();
  protected abstract VariableScopeImpl getParentVariableScope();
  protected abstract void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance);

  protected void ensureVariableInstancesInitialized() {
    if (variableInstances==null) {
      variableInstances = new HashMap<String, VariableInstanceEntity>();
      CommandContext commandContext = Context.getCommandContext();
      if (commandContext == null) {
        throw new ActivitiException("lazy loading outside command context");
      }
      List<VariableInstanceEntity> variableInstancesList = loadVariableInstances();
      for (VariableInstanceEntity variableInstance : variableInstancesList) {
        variableInstances.put(variableInstance.getName(), variableInstance);
      }
    }
  }
  
  public Map<String, Object> getVariables() {
    return collectVariables(new HashMap<String, Object>());
  }
  
  public Map<String, Object> getVariablesLocal() {
    Map<String, Object> variables = new HashMap<String, Object>();
    ensureVariableInstancesInitialized();
    for (VariableInstanceEntity variableInstance: variableInstances.values()) {
      variables.put(variableInstance.getName(), variableInstance.getValue());
    }
    return variables;
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
  
  public Object getVariableLocal(Object variableName) {
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

  public Set<String> getVariableNamesLocal() {
    ensureVariableInstancesInitialized();
    return variableInstances.keySet();
  }

  public void setVariable(String variableName, Object value) {
    if (hasVariableLocal(variableName)) {
      setVariableLocal(variableName, value);
      return;
    } 
    VariableScope parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      parentVariableScope.setVariable(variableName, value);
      return;
    }
    createVariableLocal(variableName, value);
  }
  
  public Object setVariableLocal(String variableName, Object value) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(variableName);
    if ((variableInstance != null) && (!variableInstance.getType().isAbleToStore(value))) {
      // delete variable
      removeVariable(variableName);
      variableInstance = null;
    }
    if (variableInstance == null) {
      createVariableLocal(variableName, value);
    } else {
      setVariableInstanceValue(value, variableInstance);
    }
    
    return null;
  }

  protected void setVariableInstanceValue(Object value, VariableInstanceEntity variableInstance) {
    variableInstance.setValue(value);
    
    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel==ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {
      HistoricVariableUpdateEntity historicVariableUpdate = new HistoricVariableUpdateEntity(variableInstance);
      initializeActivityInstanceId(historicVariableUpdate);
      Context
        .getCommandContext()
        .getDbSqlSession()
        .insert(historicVariableUpdate);
    }
  }
  
  protected void initializeActivityInstanceId(HistoricVariableUpdateEntity historicVariableUpdate) {
  }

  public void createVariableLocal(String variableName, Object value) {
    ensureVariableInstancesInitialized();
    
    if (variableInstances.containsKey(variableName)) {
      throw new ActivitiException("variable '"+variableName+"' already exists. Use setVariableLocal if you want to overwrite the value");
    }
    
    VariableTypes variableTypes = Context
      .getProcessEngineConfiguration()
      .getVariableTypes();
    
    VariableType type = variableTypes.findVariableType(value);
 
    VariableInstanceEntity variableInstance = VariableInstanceEntity.createAndInsert(variableName, type, value);
    initializeVariableInstanceBackPointer(variableInstance);
    variableInstances.put(variableName, variableInstance);
    
    setVariableInstanceValue(value, variableInstance);
  }

  public void createVariablesLocal(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (Map.Entry<String, ? extends Object> entry: variables.entrySet()) {
        createVariableLocal(entry.getKey(), entry.getValue());
      }
    }
  }

  public void removeVariable(String variableName) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.remove(variableName);
    if (variableInstance != null) {
      variableInstance.delete();
      return;
    }
    VariableScope parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      parentVariableScope.removeVariable(variableName);
    }
  }
  
  public void removeVariableLocal(String variableName) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.remove(variableName);
    if (variableInstance != null) {
      variableInstance.delete();
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

  public ELContext getCachedElContext() {
    return cachedElContext;
  }
  public void setCachedElContext(ELContext cachedElContext) {
    this.cachedElContext = cachedElContext;
  }
}
