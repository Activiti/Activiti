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

import java.util.List;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;

/**
 * @author Tom Baeyens
 */
public class ScriptingEngines {

  private static Logger log = Logger.getLogger(ScriptingEngines.class.getName());
  public static final String DEFAULT_SCRIPTING_LANGUAGE = "juel";

  private final ScriptEngineManager scriptEngineManager;
  protected ScriptBindingsFactory scriptBindingsFactory;

  public ScriptingEngines(ScriptBindingsFactory scriptBindingsFactory) {
    this(new ScriptEngineManager());
    this.scriptBindingsFactory = scriptBindingsFactory;
  }

  public ScriptingEngines(ScriptEngineManager scriptEngineManager) {
    this.scriptEngineManager = scriptEngineManager;
  }

  public ScriptingEngines addScriptEngineFactory(ScriptEngineFactory scriptEngineFactory) {
    scriptEngineManager.registerEngineName(scriptEngineFactory.getEngineName(), scriptEngineFactory);
    return this;
  }

  public void setScriptEngineFactories(List<ScriptEngineFactory> scriptEngineFactories) {
    if (scriptEngineFactories != null) {
      for (ScriptEngineFactory scriptEngineFactory : scriptEngineFactories) {
        scriptEngineManager.registerEngineName(scriptEngineFactory.getEngineName(), scriptEngineFactory);
      }
    }
  }

  public Object evaluate(String script, String language, VariableScope variableScope) {
    Bindings bindings = createBindings(variableScope);
    ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(language);

    if (scriptEngine == null) {
      throw new ActivitiException("Can't find scripting engine for '" + language + "'");
    }

    try {
      return scriptEngine.eval(script, bindings);
    } catch (ScriptException e) {
      throw new ActivitiException("problem evaluating script: " + e.getMessage(), e);
    }
  }

  /** override to build a spring aware ScriptingEngines */
  protected Bindings createBindings(VariableScope variableScope) {
    return scriptBindingsFactory.createBindings(variableScope); 
  }
  
  public ScriptBindingsFactory getScriptBindingsFactory() {
    return scriptBindingsFactory;
  }
  public void setScriptBindingsFactory(ScriptBindingsFactory scriptBindingsFactory) {
    this.scriptBindingsFactory = scriptBindingsFactory;
  }
}
