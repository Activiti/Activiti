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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * activity implementation of the BPMN 2.0 script task.
 * 



 */
public class ScriptTaskActivityBehavior extends TaskActivityBehavior {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ScriptTaskActivityBehavior.class);

  protected String scriptTaskId;
  protected String script;
  protected String language;
  protected String resultVariable;
  protected boolean storeScriptVariables = false; // see https://activiti.atlassian.net/browse/ACT-1626

  public ScriptTaskActivityBehavior(String script, String language, String resultVariable) {
    this.script = script;
    this.language = language;
    this.resultVariable = resultVariable;
  }

  public ScriptTaskActivityBehavior(String scriptTaskId, String script, String language, String resultVariable, boolean storeScriptVariables) {
    this(script, language, resultVariable);
    this.scriptTaskId = scriptTaskId;
    this.storeScriptVariables = storeScriptVariables;
  }

  public void execute(DelegateExecution execution) {

    ScriptingEngines scriptingEngines = Context.getProcessEngineConfiguration().getScriptingEngines();
    
    if (Context.getProcessEngineConfiguration().isEnableProcessDefinitionInfoCache()) {
      ObjectNode taskElementProperties = Context.getBpmnOverrideElementProperties(scriptTaskId, execution.getProcessDefinitionId());
      if (taskElementProperties != null && taskElementProperties.has(DynamicBpmnConstants.SCRIPT_TASK_SCRIPT)) {
        String overrideScript = taskElementProperties.get(DynamicBpmnConstants.SCRIPT_TASK_SCRIPT).asText();
        if (StringUtils.isNotEmpty(overrideScript) && !overrideScript.equals(script)) {
          script = overrideScript;
        }
      }
    }

    boolean noErrors = true;
    try {
      Object result = scriptingEngines.evaluate(script, language, execution, storeScriptVariables);

      if (resultVariable != null) {
        execution.setVariable(resultVariable, result);
      }

    } catch (ActivitiException e) {

      LOGGER.warn("Exception while executing " + execution.getCurrentFlowElement().getId() + " : " + e.getMessage());

      noErrors = false;
      Throwable rootCause = ExceptionUtils.getRootCause(e);
      if (rootCause instanceof BpmnError) {
        ErrorPropagation.propagateError((BpmnError) rootCause, execution);
      } else {
        throw e;
      }
    }
    if (noErrors) {
      leave(execution);
    }
  }

}
