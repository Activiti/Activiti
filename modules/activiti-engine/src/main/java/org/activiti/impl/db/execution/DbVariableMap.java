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
package org.activiti.impl.db.execution;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.impl.execution.VariableMap;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.task.TaskImpl;
import org.activiti.impl.variable.VariableInstance;



/**
 * @author Tom Baeyens
 */
public class DbVariableMap extends VariableMap implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  protected Map<String, VariableInstance> variableInstances = null;
  protected DbExecutionImpl execution;
  protected TaskImpl task;
  
  public DbVariableMap(DbExecutionImpl execution) {
    this.execution = execution;
    if (execution.isNew()) {
      variableInstances = new HashMap<String, VariableInstance>();
    }
  }

  public DbVariableMap(TaskImpl task) {
    this.task = task;
    if (task.isNew()) {
      variableInstances = new HashMap<String, VariableInstance>();
    }
  }

  public Object getVariable(String variableName) {    
    VariableInstance variableInstance = getInitializedVariableInstances().get(variableName);
    if (variableInstance!=null) {
      return variableInstance.getValue();
    }
    return null;
  }

  public void setVariable(String variableName, Object value) {
    VariableInstance variableInstance = getInitializedVariableInstances().get(variableName);
    if ( (variableInstance!=null)
         && (!variableInstance.getType().isAbleToStore(value))
       ) {
      // delete variable
      deleteVariable(variableName);
      variableInstance = null;
    }
    boolean isCreated = false;
    if (variableInstance==null) {
      isCreated = true;
      variableInstance = createVariableInstance(variableName, value);
    }
    variableInstance.setValue(value);
    if (isCreated) {
      insertVariableInstance(variableInstance);
    }
  }
  
  public void createVariable(String name, String type) {
    VariableInstance variableInstance = createVariableInstance(name, type);
    insertVariableInstance(variableInstance);
  }

  public Map<String, Object> getVariables() {
    Map<String, Object> variables = new HashMap<String, Object>();
    for (String variableName: getVariableNames()) {
      VariableInstance variableInstance = variableInstances.get(variableName);
      Object value = variableInstance.getValue();
      variables.put(variableName, value);
    }
    return variables;
  }

  public void deleteVariable(String variableName) {
    VariableInstance variableInstance = getInitializedVariableInstances().remove(variableName);
    if (variableInstance!=null) {
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

  protected void insertVariableInstance(VariableInstance variableInstance) {
    CommandContext
      .getCurrent()
      .getPersistenceSession()
      .insert(variableInstance);
  }

  protected Map<String, VariableInstance> getInitializedVariableInstances() {
    if (variableInstances==null) {
      List<VariableInstance> variableInstanceList = null;
      if (execution!=null) {
        variableInstanceList = CommandContext
            .getCurrent()
            .getPersistenceSession()
            .findVariablesByExecutionId(execution.getId());
      } else {
        variableInstanceList = CommandContext
            .getCurrent()
            .getPersistenceSession()
            .findVariablesByTaskId(task.getId());
      }
      variableInstances = new HashMap<String, VariableInstance>();
      for (VariableInstance variableInstance: variableInstanceList) {
        variableInstances.put(variableInstance.getName(), variableInstance);
      }
    }
    return variableInstances;
  }

  protected void addVariableInstance(VariableInstance variableInstance) {
    if (execution!=null) {
      variableInstance.setExecution(execution);
    } else {
      variableInstance.setTask(task);
    }
    variableInstances.put(variableInstance.getName(), variableInstance);
  }
  
  protected VariableInstance createVariableInstance(String variableName, Object value) {
    VariableInstance variableInstance = new VariableInstance(variableName, value);
    addVariableInstance(variableInstance);
    return variableInstance;
  }

  protected VariableInstance createVariableInstance(String name, String typeName) {
    VariableInstance variableInstance = new VariableInstance(name, typeName);
    addVariableInstance(variableInstance);
    return variableInstance;
  }
}
