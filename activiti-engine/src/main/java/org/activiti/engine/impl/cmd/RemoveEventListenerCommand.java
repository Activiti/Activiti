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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

/**
 * Command that removes an event-listener to the Activiti engine.
 * 

 */
public class RemoveEventListenerCommand implements Command<Void> {

  protected ActivitiEventListener listener;

  public RemoveEventListenerCommand(ActivitiEventListener listener) {
    super();
    this.listener = listener;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    if (listener == null) {
      throw new ActivitiIllegalArgumentException("listener is null.");
    }

    commandContext.getProcessEngineConfiguration().getEventDispatcher().removeEventListener(listener);

    return null;
  }

}
