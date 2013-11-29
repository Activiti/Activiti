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
package org.activiti.engine.delegate.event;


/**
 * An {@link ActivitiEvent} related to a single variable.
 * 
 * @author Frederik Heremans
 *
 */
public interface ActivitiVariableEvent extends ActivitiEvent {

	/**
	 * @return the name of the variable involved.
	 */
	String getVariableName();
	
	/**
	 * @return the current value if the variable.
	 */
	Object getVariableValue();
	
	
	/**
	 * @return the id of the execution the variable is set on.
	 */
	@Override
	String getExecutionId();
	
	/**
	 * @return the id of the task the variable has been set on.
	 */
	String getTaskId();
}
