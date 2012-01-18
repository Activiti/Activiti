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
package org.activiti.engine.impl.bpmn.behavior;

import javax.script.ScriptException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.event.BpmnError;
import org.activiti.engine.impl.bpmn.event.ErrorPropagation;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.scripting.ScriptingEngines;


/**
 * activity implementation of the BPMN 2.0 script task.
 * 
 * @author Joram Barrez
 * @author Christian Stettler
 */
public class ScriptTaskActivityBehavior extends TaskActivityBehavior {
  
  protected final String script;
  protected final String language;
  protected final String resultVariable;

  public ScriptTaskActivityBehavior(String script, String language, String resultVariable) {
    this.script = script;
    this.language = language;
    this.resultVariable = resultVariable;
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    ScriptingEngines scriptingEngines = Context
      .getProcessEngineConfiguration()
      .getScriptingEngines();

    boolean noErrors = true;
    try {
      Object result = scriptingEngines.evaluate(script, language, execution);
      
      if (resultVariable != null) {
        execution.setVariable(resultVariable, result);
      }

    } catch (ActivitiException e) {
      noErrors = false;
      if (e.getCause() instanceof ScriptException
          && e.getCause().getCause() instanceof ScriptException
          && e.getCause().getCause().getCause() instanceof BpmnError) {
        ErrorPropagation.propagateError((BpmnError) e.getCause().getCause().getCause(), execution);
      } else {
        throw e;
      }
    }
     if (noErrors) {
       leave(execution);
     }
  }
  
}
