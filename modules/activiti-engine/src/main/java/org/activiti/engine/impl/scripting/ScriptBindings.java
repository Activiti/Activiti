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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.SimpleScriptContext;

import org.activiti.engine.delegate.VariableScope;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ScriptBindings implements Bindings {

  /**
   * The script engine implementations put some key/value pairs into the binding.
   * This list contains those keys, such that they wouldn't be stored as process variable.
   * 
   * This list contains the keywords for JUEL, Javascript and Groovy.
   */
  protected static final Set<String> UNSTORED_KEYS = 
    new HashSet<String>(Arrays.asList("out", "out:print", "lang:import", "context", "elcontext", "print", "println", "nashorn.global"));

  protected List<Resolver> scriptResolvers;
  protected VariableScope variableScope;
  protected Bindings defaultBindings;
  protected boolean storeScriptVariables = true; // By default everything is stored (backwards compatibility) 
  
  public ScriptBindings(List<Resolver> scriptResolvers, VariableScope variableScope) {
    this.scriptResolvers = scriptResolvers;
    this.variableScope = variableScope;
    this.defaultBindings = new SimpleScriptContext().getBindings(SimpleScriptContext.ENGINE_SCOPE);
  }
  
  public ScriptBindings(List<Resolver> scriptResolvers, VariableScope variableScope, boolean storeScriptVariables) {
    this(scriptResolvers, variableScope);
    this.storeScriptVariables = storeScriptVariables;
  }

  public boolean containsKey(Object key) {
    for (Resolver scriptResolver: scriptResolvers) {
      if (scriptResolver.containsKey(key)) {
        return true;
      }
    }
    return defaultBindings.containsKey(key);
  }

  public Object get(Object key) {
    for (Resolver scriptResolver: scriptResolvers) {
      if (scriptResolver.containsKey(key)) {
        return scriptResolver.get(key);
      }
    }
    return defaultBindings.get(key);
  }

  public Object put(String name, Object value) {
    if (storeScriptVariables) {
      Object oldValue = null;
      if (!UNSTORED_KEYS.contains(name)) {
        oldValue = variableScope.getVariable(name);
        variableScope.setVariable(name, value);
        return oldValue;
      }
    }
    return defaultBindings.put(name, value);
  }

  public Set<Map.Entry<String, Object>> entrySet() {
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
    return defaultBindings.remove(key);
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
  
  public void addUnstoredKey(String unstoredKey) {
  	UNSTORED_KEYS.add(unstoredKey);
  }
  
}
