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
import java.util.Set;



/**
 * @author Tom Baeyens
 */
public class VariableMap implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  protected Map<String, Object> variableObjects = new HashMap<String, Object>();

  public Object getVariable(String variableName) {
    return variableObjects.get(variableName);
  }

  public void setVariables(Map<String, Object> variables) {
    variableObjects.putAll(variables);
  }

  public void setVariable(String variableName, Object value) {
    variableObjects.put(variableName, value);
  }

  public void deleteVariable(String variableName) {
    variableObjects.remove(variableName);
  }

  public Set<String> getVariableNames() {
    return variableObjects.keySet();
  }

  public Map<String, Object> getVariables() {
    return variableObjects;
  }

  public boolean hasVariable(String variableName) {
    return variableObjects.containsKey(variableName);
  }

  public void createVariable(String name, String type) {
    variableObjects.put(name, null);
  }
}
