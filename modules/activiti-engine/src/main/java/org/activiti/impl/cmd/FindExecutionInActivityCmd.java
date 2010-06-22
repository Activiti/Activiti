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

package org.activiti.impl.cmd;

import org.activiti.Execution;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;


/**
 * @author Joram Barrez
 */
public class FindExecutionInActivityCmd implements Command<Execution> {
  
  protected String processInstanceId;
  
  protected String activityId;
  
  public FindExecutionInActivityCmd(String processInstanceId, String activityId) {
    this.processInstanceId = processInstanceId;
    this.activityId = activityId;
  }

  public Execution execute(CommandContext commandContext) {
    ExecutionImpl execution = commandContext.getPersistenceSession().findExecution(processInstanceId);
    return execution.findExecution(activityId);
  }
  
}
