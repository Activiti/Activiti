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
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

/**
 * Command that dispatches an event.
 * 
 * @author Frederik Heremans
 */
public class DispatchEventCommand implements Command<Void> {
	
	protected ActivitiEvent event;
	
	public DispatchEventCommand(ActivitiEvent event) {
	  this.event = event;
  }

	@Override
  public Void execute(CommandContext commandContext) {
		if(event == null) {
			throw new ActivitiIllegalArgumentException("event is null");
		}
		
		if(commandContext.getEventDispatcher().isEnabled()) {
			commandContext.getEventDispatcher().dispatchEvent(event);
		} else {
			throw new ActivitiException("Message dispatcher is disabled, cannot dispatch event");
		}
		
	  return null;
  }
	
}
