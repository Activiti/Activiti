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

import org.activiti.engine.delegate.event.ActivitiActivityCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;

/**
 * An {@link org.activiti.engine.delegate.event.ActivitiActivityCancelledEvent} implementation.
 *
 * @author martin.grofcik
 */
public class ActivitiActivityCancelledEventImpl extends ActivitiActivityEventImpl implements ActivitiActivityCancelledEvent {

	protected Object cause;

	public ActivitiActivityCancelledEventImpl() {
	  super(ActivitiEventType.ACTIVITY_CANCELLED);
  }

	public void setCause(Object cause) {
	  this.cause = cause;
  }
	
	public Object getCause() {
	  return cause;
  }

}
