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

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiSignalEvent;

/**
 * An {@link ActivitiSignalEvent} implementation.
 * 
 * @author Frederik Heremans
 */
public class ActivitiSignalEventImpl extends ActivitiActivityEventImpl implements ActivitiSignalEvent {

	protected String signalName;
	protected Object signalData;
	
	public ActivitiSignalEventImpl(ActivitiEventType type) {
	  super(type);
  }

	@Override
  public String getSignalName() {
		return signalName;
  }
	
	public void setSignalName(String signalName) {
	  this.signalName = signalName;
  }

	@Override
  public Object getSignalData() {
		return signalData;
  }
	
	public void setSignalData(Object signalData) {
	  this.signalData = signalData;
  }
}
