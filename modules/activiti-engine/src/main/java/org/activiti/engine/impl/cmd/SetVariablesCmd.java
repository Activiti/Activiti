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
package org.activiti.engine.impl.cmd;

import java.util.Map;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.pvm.impl.runtime.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class SetVariablesCmd implements Command<Object> {

  protected String scopeInstanceId;
  protected Map<String, Object> variables;
  
  public SetVariablesCmd(String scopeInstanceId, Map<String, Object> variables) {
    this.scopeInstanceId = scopeInstanceId;
    this.variables = variables;
  }

  public Object execute(CommandContext commandContext) {
    ScopeInstanceImpl scopeInstance = GetVariableCmd.findScopeInstance(commandContext, scopeInstanceId);
    scopeInstance.setVariables(variables);
    return null;
  }
}

