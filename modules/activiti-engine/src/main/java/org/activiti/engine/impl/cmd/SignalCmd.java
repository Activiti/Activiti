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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.runtime.ExecutionEntity;


/**
 * @author Tom Baeyens
 */
public class SignalCmd implements Command<Object> {

  protected String executionId;
  protected String signalName;
  protected Object signalData;
  
  public SignalCmd(String executionId, String signalName, Object signalData) {
    this.executionId = executionId;
    this.signalName = signalName;
    this.signalData = signalData;
  }

  public Object execute(CommandContext commandContext) {
    ExecutionEntity execution = commandContext
      .getRuntimeSession()
      .findExecutionById(executionId);
    
    if (execution==null) {
      throw new ActivitiException("execution "+execution+" doesn't exist");
    }
    
    execution.signal(signalName, signalData);
    return null;
  }

}
