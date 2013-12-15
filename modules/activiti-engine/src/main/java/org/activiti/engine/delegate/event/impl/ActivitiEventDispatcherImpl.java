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
package org.activiti.engine.delegate.event.impl;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

/**
 * Class capable of dispatching events.
 * 
 * @author Frederik Heremans
 */
public class ActivitiEventDispatcherImpl implements ActivitiEventDispatcher {

	protected ActivitiEventSupport eventSupport;
	protected boolean enabled = true;
	
	public ActivitiEventDispatcherImpl() {
		eventSupport = new ActivitiEventSupport();
  }
	
	public void setEnabled(boolean enabled) {
	  this.enabled = enabled;
  }
	
	public boolean isEnabled() {
	  return enabled;
  }

	@Override
  public void addEventListener(ActivitiEventListener listenerToAdd) {
	  eventSupport.addEventListener(listenerToAdd);
  }

	@Override
  public void addEventListener(ActivitiEventListener listenerToAdd, ActivitiEventType... types) {
		eventSupport.addEventListener(listenerToAdd, types);
  }

	@Override
  public void removeEventListener(ActivitiEventListener listenerToRemove) {
		eventSupport.removeEventListener(listenerToRemove);
  }

	@Override
  public void dispatchEvent(ActivitiEvent event) {
		if(enabled) {
			eventSupport.dispatchEvent(event);
		}
		
		// Check if a process context is active. If so, we also call the process-definition
		// specific listeners (if any).
		if(Context.isExecutionContextActive()) {
			ProcessDefinitionEntity definition = Context.getExecutionContext().getProcessDefinition();
			if(definition != null) {
				definition.getEventSupport().dispatchEvent(event);
			}
		} else {
//			// Try getting hold of the Process definition, based on the process definition-key, if a
//			// context is active
//			CommandContext commandContext = Context.getCommandContext();
//			if(commandContext != null) {
//				commandContext.getProcessEngineConfiguration().getDeploymentManager()
//				.resolveProcessDefinition(processDefinition)
//			}
		}
  }
}
