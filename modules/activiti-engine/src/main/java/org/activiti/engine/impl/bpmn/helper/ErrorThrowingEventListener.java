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
package org.activiti.engine.impl.bpmn.helper;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * An {@link ActivitiEventListener} that throws a error event when an event is
 * dispatched to it.
 * 
 * @author Frederik Heremans
 * 
 */
public class ErrorThrowingEventListener extends BaseDelegateEventListener {
	
	protected String errorCode;

	@Override
	public void onEvent(ActivitiEvent event) {
		if(isValidEvent(event)) {
			ExecutionEntity execution = null;
			
			if (Context.isExecutionContextActive()) {
				execution = Context.getExecutionContext().getExecution();
			} else if(event.getExecutionId() != null){
				// Get the execution based on the event's execution ID instead
				execution = Context.getCommandContext().getExecutionEntityManager()
						.findExecutionById(event.getExecutionId());
			}
			
			if(execution == null) {
				throw new ActivitiException("No execution context active and event is not related to an execution. No compensation event can be thrown.");
			}
			
			try {
				ErrorPropagation.propagateError(errorCode, execution);
			} catch (Exception e) {
				throw new ActivitiException("Error while propagating error-event", e);
			}    
		}
	}

	public void setErrorCode(String errorCode) {
	  this.errorCode = errorCode;
  }

	@Override
	public boolean isFailOnException() {
		return true;
	}
}
