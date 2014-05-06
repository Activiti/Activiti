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
package org.activiti.workflow.simple.definition.form;

import org.activiti.workflow.simple.exception.SimpleWorkflowException;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A form-property with a value that is represented as a date.
 *  
 * @author Frederik Heremans
 */
@JsonTypeName("date")
public class DatePropertyDefinition extends FormPropertyDefinition {
	
	protected boolean showTime = false;
	
	/**
	 * Creates a new field, showing both time and date components.
	 */
	public DatePropertyDefinition() {
	  this(true);
  }

	public DatePropertyDefinition(boolean showTime) {
	  this.showTime = showTime;
  }

	public boolean isShowTime() {
	  return showTime;
  }
	
	public void setShowTime(boolean showTime) {
	  this.showTime = showTime;
  }
	
	@Override
	public FormPropertyDefinition clone() {
		DatePropertyDefinition clone = new DatePropertyDefinition();
		clone.setValues(this);
	  return clone;
	}
	
	@Override
	public void setValues(FormPropertyDefinition otherDefinition) {
		if(!(otherDefinition instanceof DatePropertyDefinition)) {
			throw new SimpleWorkflowException("An instance of DatePropertyDefinition is required to set values");
		}
		
		DatePropertyDefinition datePropertyDefinition = (DatePropertyDefinition) otherDefinition;
		setName(datePropertyDefinition.getName());
		setMandatory(datePropertyDefinition.isMandatory());
		setWritable(datePropertyDefinition.isWritable());
		setShowTime(datePropertyDefinition.isShowTime());
		
		setParameters(otherDefinition.cloneParameters());
	}
}