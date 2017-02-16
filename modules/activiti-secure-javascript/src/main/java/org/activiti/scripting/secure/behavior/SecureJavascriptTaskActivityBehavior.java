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
package org.activiti.scripting.secure.behavior;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.impl.bpmn.behavior.ScriptTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.scripting.secure.impl.SecureJavascriptUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Joram Barrez
 * @author Bassam Al-Sarori
 */
public class SecureJavascriptTaskActivityBehavior extends ScriptTaskActivityBehavior {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SecureJavascriptTaskActivityBehavior.class);

    public SecureJavascriptTaskActivityBehavior(String scriptTaskId, String script,
                                            String language, String resultVariable, boolean storeScriptVariables) {
        super(scriptTaskId, script, language, resultVariable, storeScriptVariables);
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {
      ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) execution.getEngineServices().getProcessEngineConfiguration();

        if (Context.getProcessEngineConfiguration().isEnableProcessDefinitionInfoCache()) {
            ObjectNode taskElementProperties = Context.getBpmnOverrideElementProperties(scriptTaskId, execution.getProcessDefinitionId());
            if (taskElementProperties != null && taskElementProperties.has(DynamicBpmnConstants.SCRIPT_TASK_SCRIPT)) {
                String overrideScript = taskElementProperties.get(DynamicBpmnConstants.SCRIPT_TASK_SCRIPT).asText();
                if (StringUtils.isNotEmpty(overrideScript) && overrideScript.equals(script) == false) {
                    script = overrideScript;
                }
            }
        }

      boolean noErrors = true;
      try {
    	Object result = SecureJavascriptUtil.evaluateScript(execution, script, config.getBeans());
        if (resultVariable != null) {
          execution.setVariable(resultVariable, result);
        }

      } catch (ActivitiException e) {

        LOGGER.warn("Exception while executing " + execution.getActivity().getId() + " : " + e.getMessage());

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
