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
package org.activiti.scripting.secure.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.listener.ScriptExecutionListener;
import org.activiti.scripting.secure.behavior.SecureJavascriptTaskParseHandler;
import org.activiti.scripting.secure.impl.SecureJavascriptUtil;

/**
 * @author Joram Barrez
 */
public class SecureJavascriptExecutionListener extends ScriptExecutionListener {

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    validateParameters();
    if (SecureJavascriptTaskParseHandler.LANGUAGE_JAVASCRIPT.equalsIgnoreCase(language.getValue(execution).toString())) {
      Object result = SecureJavascriptUtil.evaluateScript(execution, script.getExpressionText());
      
      if (resultVariable != null) {
        execution.setVariable(resultVariable.getExpressionText(), result);
      }
    } else {
      super.notify(execution);
    }
  }
  
}
