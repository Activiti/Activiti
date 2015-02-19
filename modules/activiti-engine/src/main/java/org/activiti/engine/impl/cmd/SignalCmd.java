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

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

import java.util.Map;

/**
 * @author Tom Baeyens
 */
public class SignalCmd extends NeedsActiveExecutionCmd<Object> {

  private static final long serialVersionUID = 1L;
  
  protected String signalName;
  protected Object signalData;
  protected final Map<String, Object> processVariables;
  
  public SignalCmd(String executionId, String signalName, Object signalData, Map<String, Object> processVariables) {
    super(executionId);
    this.signalName = signalName;
    this.signalData = signalData;
    this.processVariables = processVariables;
  }
  
  protected Object execute(CommandContext commandContext, ExecutionEntity execution) {
    if(processVariables != null) {
      execution.setVariables(processVariables);
    }
    execution.signal(signalName, signalData);
    return null;
  }
  
  @Override
  protected String getSuspendedExceptionMessage() {
    return "Cannot signal an execution that is suspended";
  }

}
