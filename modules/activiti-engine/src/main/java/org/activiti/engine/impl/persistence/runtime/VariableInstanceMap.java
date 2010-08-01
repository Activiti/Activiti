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
public abstract class VariableInstanceMap implements Serializable {

  private static final long serialVersionUID = 1L;

  protected Map<String, VariableInstanceEntity> variableInstances = null;

  protected abstract List<VariableInstanceEntity> findVariableInstanceList();
  protected abstract VariableInstanceEntity createVariableInstance(String variableName, Type type, Object value);
  
  public Object getVariable(String variableName) {
    VariableInstanceEntity variableInstanceEntity = getInitializedVariableInstances().get(variableName);
    if (variableInstanceEntity != null) {
      return variableInstanceEntity.getValue();
    }
    return null;
  }

  public void setVariable(String variableName, Object value) {
    VariableInstanceEntity variableInstanceEntity = getInitializedVariableInstances().get(variableName);
    if ((variableInstanceEntity != null) && (!variableInstanceEntity.getType().isAbleToStore(value))) {
      // delete variable
      deleteVariable(variableName);
      variableInstanceEntity = null;
    }
    if (variableInstanceEntity == null) {
      variableInstanceEntity = createVariableInstance(variableName, value);
    }
  }

  public Map<String, Object> getVariables() {
    Map<String, Object> variables = new HashMap<String, Object>();
    for (String variableName : getVariableNames()) {
      VariableInstanceEntity variableInstanceEntity = variableInstances.get(variableName);
      Object value = variableInstanceEntity.getValue();
      variables.put(variableName, value);
    }
    return variables;
  }

  public void deleteVariable(String variableName) {
    VariableInstanceEntity variableInstanceEntity = getInitializedVariableInstances().remove(variableName);
    if (variableInstanceEntity != null) {
      variableInstanceEntity.delete();
    }
  }

  public Set<String> getVariableNames() {
    return getInitializedVariableInstances().keySet();
  }

  public boolean hasVariable(String variableName) {
    return getInitializedVariableInstances().containsKey(variableName);
  }

  // variable instance methods ////////////////////////////////////////////////

  protected void insertVariableInstance(VariableInstanceEntity variableInstanceEntity) {
    CommandContext
      .getCurrent()
      .getRuntimeSession()
      .insertVariableInstance(variableInstanceEntity);
  }

  protected Map<String, VariableInstanceEntity> getInitializedVariableInstances() {
    if (variableInstances == null) {
      List<VariableInstanceEntity> variableInstanceList = null;
      variableInstanceList = findVariableInstanceList();
      variableInstances = new HashMap<String, VariableInstanceEntity>();
      for (VariableInstanceEntity variableInstanceEntity : variableInstanceList) {
        variableInstances.put(variableInstanceEntity.getName(), variableInstanceEntity);
      }
    }
    return variableInstances;
  }

  protected void addVariableInstance(VariableInstanceEntity variableInstanceEntity) {
    variableInstances.put(variableInstanceEntity.getName(), variableInstanceEntity);
  }

  protected VariableInstanceEntity createVariableInstance(String variableName, Object value) {
    VariableTypes variableTypes = CommandContext
      .getCurrent()
      .getProcessEngineConfiguration()
      .getVariableTypes();
    
    Type type = variableTypes.findVariableType(value);
    
    return createVariableInstance(variableName, type, value);
  }
}
