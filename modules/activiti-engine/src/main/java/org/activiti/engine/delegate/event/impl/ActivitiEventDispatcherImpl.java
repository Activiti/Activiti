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

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;

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
		if (enabled) {
			eventSupport.dispatchEvent(event);
		}

		// Check if a process context is active. If so, we also call the
		// process-definition specific listeners (if any).
		if (Context.isExecutionContextActive()) {
			ProcessDefinitionEntity definition = Context.getExecutionContext().getProcessDefinition();
			if (definition != null) {
				definition.getEventSupport().dispatchEvent(event);
			}
		} else {
			// Try getting hold of the Process definition, based on the process
			// definition-key, if a context is active
			CommandContext commandContext = Context.getCommandContext();
			if (commandContext != null) {
				ProcessDefinitionEntity processDefinition = extractProcessDefinitionEntityFromEvent(event);
				if (processDefinition != null) {
					processDefinition.getEventSupport().dispatchEvent(event);
				}
			}
		}
	}

	/**
	 * In case no process-context is active, this method attempts to extract a
	 * process-definition based on the event. In case it's an event related to an
	 * entity, this can be deducted by inspecting the entity, without additional
	 * queries to the database.
	 * 
	 * If not an entity-related event, the process-definition will be retrieved
	 * based on the processDefinitionId (if filled in). This requires an
	 * additional query to the database in case not already cached. However,
	 * queries will only occur when the definition is not yet in the cache, which
	 * is very unlikely to happen, unless evicted.
	 * 
	 * @param event
	 * @return
	 */
	protected ProcessDefinitionEntity extractProcessDefinitionEntityFromEvent(ActivitiEvent event) {
		ProcessDefinitionEntity result = null;

		if (event.getProcessDefinitionId() != null) {
			result = Context.getProcessEngineConfiguration().getDeploymentManager().getProcessDefinitionCache()
			    .get(event.getProcessDefinitionId());
			if (result != null) {
				result = Context.getProcessEngineConfiguration().getDeploymentManager().resolveProcessDefinition(result);
			}
		}

		if(result == null && event instanceof ActivitiEntityEvent) {
			Object entity = ((ActivitiEntityEvent) event).getEntity();
			if(entity instanceof ProcessDefinition) {
				result = (ProcessDefinitionEntity) entity;
			}
		}
		return result;
	}

}
