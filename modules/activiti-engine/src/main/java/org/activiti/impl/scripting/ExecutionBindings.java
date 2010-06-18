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
package org.activiti.impl.scripting;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import org.activiti.ActivitiException;
import org.activiti.impl.execution.ExecutionImpl;


/**
 * @author Tom Baeyens
 */
public class ExecutionBindings implements Bindings {

  ExecutionImpl execution;
  
  public ExecutionBindings(ExecutionImpl execution) {
    if (execution==null) {
      throw new ActivitiException("execution cannot be null");
    }
    this.execution = execution;
  }

  public boolean containsKey(Object key) {
    return execution.hasVariable((String) key);
  }

  public Object get(Object key) {
    return execution.getVariable((String) key);
  }

  public Object put(String name, Object value) {
    // juel script engine sets several vars
    // execution.setVariable(name, value);
    return null;
  }

  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    return execution.getVariables().entrySet();
  }

  public Set<String> keySet() {
    return execution.getVariables().keySet();
  }

  public int size() {
    return execution.getVariables().size();
  }

  public Collection<Object> values() {
    return execution.getVariables().values();
  }

  public void putAll(Map< ? extends String, ? extends Object> toMerge) {
    throw new UnsupportedOperationException();
  }

  public Object remove(Object key) {
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
