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

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;

/**
 * @author Tom Baeyens
 */
public class ScriptingEngines {

  public static final String DEFAULT_SCRIPTING_LANGUAGE = "juel";

  private final ScriptEngineManager scriptEngineManager;

  public ScriptingEngines() {
    this(new ScriptEngineManager());
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

  public Object evaluate(String script, String language, DelegateExecution execution) {
    Bindings bindings = createBindings(execution);
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
  protected Bindings createBindings(DelegateExecution execution) {
    if (execution != null) {
      return new ExecutionBindings((ExecutionImpl) execution);
    }
    return new SimpleBindings();
  }
}
