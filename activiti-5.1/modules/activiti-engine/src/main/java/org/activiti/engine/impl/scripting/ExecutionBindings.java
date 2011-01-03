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
package org.activiti.engine.impl.scripting;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;


/**
 * Bindings implementation using an {@link ExecutionImpl} as 'back-end'.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ExecutionBindings implements Bindings {

  protected static final String EXECUTION_KEY = "execution";
  
  /**
   * The script engine implementations put some key/value pairs into the binding.
   * This list contains those keys, such that they wouldn't be stored as process variable.
   * 
   * This list contains the keywords for JUEL, Javascript and Groovy.
   */
  protected static final Set<String> UNSTORED_KEYS = 
    new HashSet<String>(Arrays.asList("out", "out:print", "lang:import", "context", "elcontext", "print", "println"));
  
  protected VariableScope variableScope;
  
  public ExecutionBindings(VariableScope variableScope) {
    if (variableScope==null) {
      throw new ActivitiException("variableScope cannot be null");
    }
    this.variableScope = variableScope;
  }

  public boolean containsKey(Object key) {
    return EXECUTION_KEY.equals(key) || variableScope.hasVariable((String) key);
  }

  public Object get(Object key) {
    if (EXECUTION_KEY.equals(key)) {
      return variableScope;
    }
    return variableScope.getVariable((String) key);
  }

  public Object put(String name, Object value) {
    Object oldValue = null;
    if (!UNSTORED_KEYS.contains(name)) {
      oldValue = variableScope.getVariable(name);
      variableScope.setVariable(name, value);
    }
    return oldValue;
  }

  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    return variableScope.getVariables().entrySet();
  }

  public Set<String> keySet() {
    return variableScope.getVariables().keySet();
  }

  public int size() {
    return variableScope.getVariables().size();
  }

  public Collection<Object> values() {
    return variableScope.getVariables().values();
  }

  public void putAll(Map< ? extends String, ? extends Object> toMerge) {
    throw new UnsupportedOperationException();
  }

  public Object remove(Object key) {
    if (UNSTORED_KEYS.contains(key)) {
      return null;
    }
    throw new UnsupportedOperationException();
  }

  public void clear() {
    throw new UnsupportedOperationException();
  }

  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException();
  }

  public boolean isEmpty() {
    throw new UnsupportedOperationException();
  }
}
