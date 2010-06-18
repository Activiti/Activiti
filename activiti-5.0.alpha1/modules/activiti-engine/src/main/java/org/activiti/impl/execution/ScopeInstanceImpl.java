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
package org.activiti.impl.execution;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.impl.definition.TypeSet;
import org.activiti.impl.definition.VariableDeclaration;
import org.activiti.impl.variable.VariableInstance;



/**
 * @author Tom Baeyens
 */
public class ScopeInstanceImpl implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  protected String id;

  protected Map<String, VariableInstance> variableInstances = new HashMap<String, VariableInstance>();

  public Object getVariable(String variableName) {
    VariableInstance variableInstance = variableInstances.get(variableName);
    if (variableInstance!=null) {
      return variableInstance.getValue();
    }
    return null;
  }

  public void setVariables(Map<String, Object> variables) {
    if (variables!=null) {
      for (Map.Entry<String, Object> entry: variables.entrySet()) {
        setVariable(entry.getKey(), entry.getValue());
      }
    }
  }

  public void setVariable(String variableName, Object value) {
    VariableInstance variableInstance = variableInstances.get(variableName);
    if (variableInstance!=null) {
      if (variableInstance.isAbleToStore(value)) {
        variableInstance.setValue(value);
      } else {
        // delete variable
        deleteVariable(variableName);
        variableInstance = null;
      }
    }
    if (variableInstance==null) {
      variableInstance = createVariableInstance(variableName, value);
    }
    
    variableInstance.setValue(value);
  }

  protected VariableInstance createVariableInstance(String variableName, Object value) {
    VariableInstance variableInstance = null;
    VariableDeclaration variableDeclaration = getVariableDeclaration(variableName);
    // if there is a variable type declaration, use that
    if ( (variableDeclaration!=null)
         && (variableDeclaration.getTypeName()!=null)
       ) {
      String typeName = variableDeclaration.getTypeName();
      variableInstance = TypeSet.INSTANCE.createVariableInstance(typeName);
      
    } else { 
      // else use dynamic type detection, create variable instance and then set value.
      variableInstance = TypeSet.INSTANCE.createVariableInstance(value);
    }
    variableInstance.setScopeInstance(this);
    variableInstances.put(variableName, variableInstance);
    return variableInstance;
  }
  
  public void deleteVariable(String variableName) {
    VariableInstance variableInstance = variableInstances.remove(variableName);
    if (variableInstance!=null) {
      variableInstance.setScopeInstance(null);
    }
  }
  
  public VariableDeclaration getVariableDeclaration(String variableName) {
    // TODO finish this
    return null;
  }

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }  
}
