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
package org.activiti.engine.impl.persistence.runtime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.variable.Type;
import org.activiti.engine.impl.variable.VariableTypes;

/**
 * @author Tom Baeyens
 */
public class DbVariableMap extends VariableMap implements Serializable {

  private static final long serialVersionUID = 1L;

  private Map<String, VariableInstanceEntity> variableInstances = null;
  private final ExecutionEntity execution;

  public DbVariableMap(ExecutionEntity execution) {
    this.execution = execution;
    if (execution.isNew()) {
      variableInstances = new HashMap<String, VariableInstanceEntity>();
    }
  }

  public Object getVariable(String variableName) {
    VariableInstanceEntity variableInstance = getInitializedVariableInstances().get(variableName);
    if (variableInstance != null) {
      return variableInstance.getValue();
    }
    return null;
  }

  public void setVariable(String variableName, Object value) {
    VariableInstanceEntity variableInstance = getInitializedVariableInstances().get(variableName);
    if ((variableInstance != null) && (!variableInstance.getType().isAbleToStore(value))) {
      // delete variable
      deleteVariable(variableName);
      variableInstance = null;
    }
    boolean isCreated = false;
    if (variableInstance == null) {
      isCreated = true;
      variableInstance = createVariableInstance(variableName, value);
    }
    variableInstance.setValue(value);
    if (isCreated) {
      insertVariableInstance(variableInstance);
    }
  }

  public Map<String, Object> getVariables() {
    Map<String, Object> variables = new HashMap<String, Object>();
    for (String variableName : getVariableNames()) {
      VariableInstanceEntity variableInstance = variableInstances.get(variableName);
      Object value = variableInstance.getValue();
      variables.put(variableName, value);
    }
    return variables;
  }

  public void deleteVariable(String variableName) {
    VariableInstanceEntity variableInstance = getInitializedVariableInstances().remove(variableName);
    if (variableInstance != null) {
      variableInstance.delete();
    }
  }

  public Set<String> getVariableNames() {
    return getInitializedVariableInstances().keySet();
  }

  public boolean hasVariable(String variableName) {
    return getInitializedVariableInstances().containsKey(variableName);
  }

  // variable instance methods ////////////////////////////////////////////////

  protected void insertVariableInstance(VariableInstanceEntity variableInstance) {
    CommandContext
      .getCurrent()
      .getDbSqlSession()
      .insert(variableInstance);
  }

  protected Map<String, VariableInstanceEntity> getInitializedVariableInstances() {
    if (variableInstances == null) {
      List<VariableInstanceEntity> variableInstanceList = null;
      variableInstanceList = CommandContext
          .getCurrent()
          .getRuntimeSession()
          .findVariableInstancesByExecutionId(execution.getId());
      variableInstances = new HashMap<String, VariableInstanceEntity>();
      for (VariableInstanceEntity variableInstance : variableInstanceList) {
        variableInstances.put(variableInstance.getName(), variableInstance);
      }
    }
    return variableInstances;
  }

  protected void addVariableInstance(VariableInstanceEntity variableInstance) {
    variableInstance.setExecution(execution);
    variableInstances.put(variableInstance.getName(), variableInstance);
  }

  protected VariableInstanceEntity createVariableInstance(String variableName, Object value) {
    VariableTypes variableTypes = CommandContext
      .getCurrent()
      .getProcessEngineConfiguration()
      .getVariableTypes();
    Type type = variableTypes.findVariableType(value);
    VariableInstanceEntity variableInstance = new VariableInstanceEntity(type, variableName, value);
    addVariableInstance(variableInstance);
    return variableInstance;
  }

}
