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

package org.activiti.engine.impl.bpmn.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.Expression;
import org.activiti.engine.impl.scripting.ScriptingEngines;

public class ScriptExecutionListener implements ExecutionListener {
  
  private static final long serialVersionUID = 1L;

  protected Expression script;

  protected Expression language = null;

  protected Expression resultVariable = null;

	@Override
  public void notify(DelegateExecution execution) throws Exception {
		validateParameters();

		ScriptingEngines scriptingEngines = Context.getProcessEngineConfiguration().getScriptingEngines();

		Object result = scriptingEngines.evaluate(script.getExpressionText(), language.getValue(execution).toString(), execution);

		if (resultVariable != null) {
		  execution.setVariable(resultVariable.getExpressionText(), result);
		}
	}

  protected void validateParameters() {
    if (script == null) {
			throw new IllegalArgumentException("The field 'script' should be set on the ExecutionListener");
		}

		if (language == null) {
			throw new IllegalArgumentException("The field 'language' should be set on the ExecutionListener");
		}
  }

	public void setScript(Expression script) {
		this.script = script;
	}

	public void setLanguage(Expression language) {
		this.language = language;
	}

	public void setResultVariable(Expression resultVariable) {
		this.resultVariable = resultVariable;
	}
}
