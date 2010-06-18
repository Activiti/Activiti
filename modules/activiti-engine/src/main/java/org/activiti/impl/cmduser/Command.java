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
package org.activiti.impl.cmduser;

import org.activiti.ProcessEngine;
import org.activiti.ProcessService;

/**
 * When the operations offered through the services are insufficient, new
 * functionality or a combination of functionlity can be obtained by subclassing
 * this class, and creating a custom 'command'.
 * 
 * These commands can be executed by handing them over to the command execute
 * operations of the {@link org.activiti.ProcessEngine}. The command will then
 * be passed through the configured interceptor stack (eg to start a new
 * transaction) and is executed by the commandExecutor in the same environment
 * as the operations offered through the Service API.
 * 
 * @author Tom Baeyens
 */
public abstract class Command<T> {
  
  private Object[] parameters = null;
  
  public Command() {
  }
  
  public Command(Object... parameters) {
    this.parameters = parameters;
  }
  
  public Object getParameter(int index) {
    return parameters[index];
  }

  public abstract T execute(ProcessService processService) throws Exception;
}
