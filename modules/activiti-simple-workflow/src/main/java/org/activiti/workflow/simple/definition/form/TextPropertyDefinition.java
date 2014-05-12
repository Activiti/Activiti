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
 * A form-property with a value that is represented as a simple text.
 *  
 * @author Frederik Heremans
 */
@JsonTypeName("text")
public class TextPropertyDefinition extends FormPropertyDefinition {
	
	protected boolean multiline = false;

	/**
	 * Creates a single-lined text-property.
	 */
	public TextPropertyDefinition() {
		this(false);
  }
	
	public TextPropertyDefinition(boolean multiline) {
	  this.multiline = multiline;
  }

	public void setMultiline(boolean multiline) {
	  this.multiline = multiline;
  }
	
	public boolean isMultiline() {
	  return multiline;
  }
	
	@Override
	public FormPropertyDefinition clone() {
		TextPropertyDefinition clone = new TextPropertyDefinition();
		clone.setValues(this);
	  return clone;
	}
	
	@Override
	public void setValues(FormPropertyDefinition otherDefinition) {
		if(!(otherDefinition instanceof TextPropertyDefinition)) {
			throw new SimpleWorkflowException("An instance of TextPropertyDefinition is required to set values");
		}
		
		TextPropertyDefinition textDefinition = (TextPropertyDefinition) otherDefinition;
		setName(textDefinition.getName());
		setMandatory(textDefinition.isMandatory());
		setWritable(textDefinition.isWritable());
		setMultiline(textDefinition.isMultiline());
		
		setParameters(otherDefinition.cloneParameters());
	}
}
