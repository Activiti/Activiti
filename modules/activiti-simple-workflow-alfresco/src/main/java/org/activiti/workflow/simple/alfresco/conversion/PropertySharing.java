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
package org.activiti.workflow.simple.alfresco.conversion;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class which allows registering property sharing for a specific user-task. Both incoming (properties 
 * copied from worklfow to task when task is created) and outgoing (properties copied from task to workflow
 * when task is finished) sharing is supported
 * 
 * @author Frederik Heremans
 */
public class PropertySharing {

	protected String userTaskId;
	
	protected Map<String, String> incomingProperties = new LinkedHashMap<String, String>();
	protected Map<String, String> outgoingProperties = new LinkedHashMap<String, String>();
	
	public void addIncomingProperty(String workflowPropertyName, String taskPropertyName) {
		incomingProperties.put(workflowPropertyName, taskPropertyName);
	}
	
	public void addOutgoingProperty(String workflowPropertyName, String taskPropertyName) {
		outgoingProperties.put(workflowPropertyName, taskPropertyName);
	}
	
	public Map<String, String> getIncomingProperties() {
	  return incomingProperties;
  }
	
	public Map<String, String> getOutgoingProperties() {
	  return outgoingProperties;
  }

	public String getUserTaskId() {
	  return userTaskId;
  }
	
	public void setUserTaskId(String userTaskId) {
	  this.userTaskId = userTaskId;
  }
	
	public boolean hasIncomingProperties() {
		return incomingProperties != null && !incomingProperties.isEmpty();
	}
	
	public boolean hasOutgoingProperties() {
		return outgoingProperties != null && !outgoingProperties.isEmpty();
	}
}
