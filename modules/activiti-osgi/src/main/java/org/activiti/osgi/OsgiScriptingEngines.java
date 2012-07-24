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
package org.activiti.osgi;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.scripting.ScriptBindingsFactory;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.osgi.framework.InvalidSyntaxException;

/**
 * @author Tijs Rademakers
 */
public class OsgiScriptingEngines extends ScriptingEngines {

  public OsgiScriptingEngines(ScriptBindingsFactory scriptBindingsFactory) {
    super(scriptBindingsFactory);
  }

  public OsgiScriptingEngines(ScriptEngineManager scriptEngineManager) {
    super(scriptEngineManager);
  }

  public Object evaluate(String script, String language, VariableScope variableScope) {
    Bindings bindings = createBindings(variableScope);
    ScriptEngine scriptEngine = null;
    try {
      scriptEngine = Extender.resolveScriptEngine(language);
    } catch (InvalidSyntaxException e) {
      throw new ActivitiException("problem resolving scripting engine" + e.getMessage(), e);
    }
    
    if (scriptEngine == null) {
      throw new ActivitiException("Can't find scripting engine for '" + language + "'");
    }

    try {
      return scriptEngine.eval(script, bindings);
    } catch (ScriptException e) {
      throw new ActivitiException("problem evaluating script: " + e.getMessage(), e);
    }
  }
}
