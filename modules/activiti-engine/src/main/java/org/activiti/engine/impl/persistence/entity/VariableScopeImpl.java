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
import java.util.Stack;

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
    setVariable(variableName, value, newStack());
  }
  
  private Stack<VariableScopeImpl> newStack() {    
    Stack<VariableScopeImpl> stack = new Stack<VariableScopeImpl>();
    stack.add(this);
    return stack;
  }
  
  /**
   * Variable used within this VariableScope to remember on which scope we initially called "setVariable" before
   * we maybe delegated to parent scopes. We need this information to correctly fill the history
   * (see http://jira.codehaus.org/browse/ACT-1083) 
   */
  private void setVariable(String variableName, Object value, Stack<VariableScopeImpl> scopes) {    
    if (hasVariableLocal(variableName)) {
      setVariableLocal(variableName, value, scopes);
      return;
    } 
    VariableScopeImpl parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      scopes.add(parentVariableScope);
      parentVariableScope.setVariable(variableName, value, scopes);
      return;
    }
    createVariableLocal(variableName, value, scopes);
  }

  public Object setVariableLocal(String variableName, Object value) {
    return setVariableLocal(variableName, value, newStack());    
  }
  
  public Object setVariableLocal(String variableName, Object value, Stack<VariableScopeImpl> scopes) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(variableName);
    if ((variableInstance != null) && (!variableInstance.getType().isAbleToStore(value))) {
      // delete variable
      removeVariable(variableName);
      variableInstance = null;
    }
    if (variableInstance == null) {
      createVariableLocal(variableName, value, scopes);
    } else {
      setVariableInstanceValue(value, variableInstance, scopes);
    }
    
    return null;
  }

  protected void setVariableInstanceValue(Object value, VariableInstanceEntity variableInstance, Stack<VariableScopeImpl> scopes) {
    variableInstance.setValue(value);
    
    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel == ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {
      HistoricVariableUpdateEntity historicVariableUpdate = new HistoricVariableUpdateEntity(variableInstance);
      initializeActivityInstanceId(historicVariableUpdate, scopes);      
      Context
        .getCommandContext()
        .getDbSqlSession()
        .insert(historicVariableUpdate);
    }
  }
  
  protected void initializeActivityInstanceId(HistoricVariableUpdateEntity historicVariableUpdate, Stack<VariableScopeImpl> scopes) {    
  }

  protected void removeVariableInstanceValue(VariableInstanceEntity variableInstance, Stack<VariableScopeImpl> scopes) {
    variableInstance.delete();
    variableInstance.setValue(null);
    
    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel == ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {
      HistoricVariableUpdateEntity historicVariableUpdate = new HistoricVariableUpdateEntity(variableInstance);
      initializeActivityInstanceId(historicVariableUpdate, scopes);
      Context
        .getCommandContext()
        .getDbSqlSession()
        .insert(historicVariableUpdate);
    }
  }
  
  public void createVariableLocal(String variableName, Object value) {
    createVariableLocal(variableName, value, newStack());
  }
  
  public void createVariableLocal(String variableName, Object value, Stack<VariableScopeImpl> scopes) {
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
    
    setVariableInstanceValue(value, variableInstance, scopes);
  }

  public void removeVariable(String variableName) {
    removeVariable(variableName, newStack());
  }
  
  /**
   * Variable used within this VariableScope to remember on which scope we initially called "setVariable" before
   * we maybe delegated to parent scopes. We need this information to correctly fill the history
   * (see http://jira.codehaus.org/browse/ACT-1083) 
   */
  protected void removeVariable(String variableName, Stack<VariableScopeImpl> scopes) {
    if (hasVariableLocal(variableName)) {
      removeVariableLocal(variableName, scopes);
      return;
    }
    VariableScopeImpl parentVariableScope = getParentVariableScope();
    if (parentVariableScope != null) {
      scopes.add(parentVariableScope);
      parentVariableScope.removeVariable(variableName, scopes);
      return;
    }
  }
  
  public void removeVariableLocal(String variableName) {
    removeVariable(variableName, newStack());
  }
  
  protected void removeVariableLocal(String variableName, Stack<VariableScopeImpl> scopes) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.remove(variableName);
    if (variableInstance != null) {
      removeVariableInstanceValue(variableInstance, scopes);
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
  
  public void deleteVariablesLocal() {
    List<String> variableNames = new ArrayList<String>(getVariableNamesLocal());
    for (String variableName : variableNames) {
      VariableInstanceEntity variableInstance = variableInstances.remove(variableName);
      if (variableInstance != null) {
        variableInstance.delete();
      }
    }
  }
  
}
