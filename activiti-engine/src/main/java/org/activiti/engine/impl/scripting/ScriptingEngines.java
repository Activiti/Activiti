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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;

/**



 */
public class ScriptingEngines {

  public static final String DEFAULT_SCRIPTING_LANGUAGE = "juel";
  public static final String GROOVY_SCRIPTING_LANGUAGE = "groovy";

  private final ScriptEngineManager scriptEngineManager;
  protected ScriptBindingsFactory scriptBindingsFactory;

  protected boolean cacheScriptingEngines = true;
  protected Map<String, ScriptEngine> cachedEngines;

  public ScriptingEngines(ScriptBindingsFactory scriptBindingsFactory) {
    this(new ScriptEngineManager());
    this.scriptBindingsFactory = scriptBindingsFactory;
  }

  public ScriptingEngines(ScriptEngineManager scriptEngineManager) {
    this.scriptEngineManager = scriptEngineManager;
    cachedEngines = new HashMap<String, ScriptEngine>();
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
    return evaluate(script, language, createBindings(variableScope));
  }

  public Object evaluate(String script, String language, VariableScope variableScope, boolean storeScriptVariables) {
    return evaluate(script, language, createBindings(variableScope, storeScriptVariables));
  }

  public void setCacheScriptingEngines(boolean cacheScriptingEngines) {
    this.cacheScriptingEngines = cacheScriptingEngines;
  }

  public boolean isCacheScriptingEngines() {
    return cacheScriptingEngines;
  }

  protected Object evaluate(String script, String language, Bindings bindings) {
    ScriptEngine scriptEngine = getEngineByName(language);
    try {
      return scriptEngine.eval(script, bindings);
    } catch (ScriptException e) {
      throw new ActivitiException("problem evaluating script: " + e.getMessage(), e);
    }
  }

  protected ScriptEngine getEngineByName(String language) {
    ScriptEngine scriptEngine = null;

    if (cacheScriptingEngines) {
      scriptEngine = cachedEngines.get(language);
      if (scriptEngine == null) {
        scriptEngine = scriptEngineManager.getEngineByName(language);

        if (scriptEngine != null) {
          // ACT-1858: Special handling for groovy engine regarding GC
          if (GROOVY_SCRIPTING_LANGUAGE.equals(language)) {
            try {
              scriptEngine.getContext().setAttribute("#jsr223.groovy.engine.keep.globals", "weak", ScriptContext.ENGINE_SCOPE);
            } catch (Exception ignore) {
              // ignore this, in case engine doesn't support the
              // passed attribute
            }
          }

          // Check if script-engine allows caching, using "THREADING"
          // parameter as defined in spec
          Object threadingParameter = scriptEngine.getFactory().getParameter("THREADING");
          if (threadingParameter != null) {
            // Add engine to cache as any non-null result from the
            // threading-parameter indicates at least MT-access
            cachedEngines.put(language, scriptEngine);
          }
        }
      }
    } else {
      scriptEngine = scriptEngineManager.getEngineByName(language);
    }

    if (scriptEngine == null) {
      throw new ActivitiException("Can't find scripting engine for '" + language + "'");
    }
    return scriptEngine;
  }

  /** override to build a spring aware ScriptingEngines */
  protected Bindings createBindings(VariableScope variableScope) {
    return scriptBindingsFactory.createBindings(variableScope);
  }

  /** override to build a spring aware ScriptingEngines */
  protected Bindings createBindings(VariableScope variableScope, boolean storeScriptVariables) {
    return scriptBindingsFactory.createBindings(variableScope, storeScriptVariables);
  }

  public ScriptBindingsFactory getScriptBindingsFactory() {
    return scriptBindingsFactory;
  }

  public void setScriptBindingsFactory(ScriptBindingsFactory scriptBindingsFactory) {
    this.scriptBindingsFactory = scriptBindingsFactory;
  }
}
