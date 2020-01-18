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
package org.activiti.bpmn.model;

public class TerminateEventDefinition extends EventDefinition {
	
	/**
	 * When true, this event will terminate all parent process instances (in the case of using call activity),
	 * thus ending the whole process instance.
	 * 
	 * By default false (BPMN spec compliant): the parent scope is terminated (subprocess: embedded or call activity)
	 */
	protected boolean terminateAll;
	
	/**
	 * When true (and used within a multi instance), this event will terminate all multi instance instances 
	 * of the embedded subprocess/call activity this event is used in. 
	 * 
	 * In case of nested multi instance, only the first parent multi instance structure will be destroyed.
	 * In case of 'true' and not being in a multi instance construction: executes the default behavior.
	 * 
	 * Note: if terminate all is set to true, this will have precedence over this.
	 */
	protected boolean terminateMultiInstance;

  public TerminateEventDefinition clone() {
    TerminateEventDefinition clone = new TerminateEventDefinition();
    clone.setValues(this);
    return clone;
  }

  public void setValues(TerminateEventDefinition otherDefinition) {
    super.setValues(otherDefinition);
    this.terminateAll = otherDefinition.isTerminateAll();
    this.terminateMultiInstance = otherDefinition.isTerminateMultiInstance();
  }

	public boolean isTerminateAll() {
		return terminateAll;
	}

	public void setTerminateAll(boolean terminateAll) {
		this.terminateAll = terminateAll;
	}

  public boolean isTerminateMultiInstance() {
    return terminateMultiInstance;
  }

  public void setTerminateMultiInstance(boolean terminateMultiInstance) {
    this.terminateMultiInstance = terminateMultiInstance;
  }
	
}
