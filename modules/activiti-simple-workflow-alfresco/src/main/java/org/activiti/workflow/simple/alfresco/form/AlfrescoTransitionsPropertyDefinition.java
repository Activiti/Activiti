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
package org.activiti.workflow.simple.alfresco.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyEntry;
import org.activiti.workflow.simple.exception.SimpleWorkflowException;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class AlfrescoTransitionsPropertyDefinition extends FormPropertyDefinition {

protected List<ListPropertyEntry> entries = new ArrayList<ListPropertyEntry>();
	
	public void setTransitions(List<ListPropertyEntry> entries) {
	  this.entries = entries;
  }
	
	@JsonSerialize(contentAs=ListPropertyEntry.class)
	public List<ListPropertyEntry> getTransitions() {
	  return entries;
  }
	
	public void addEntry(ListPropertyEntry entry) {
		entries.add(entry);
	}
	
	@Override
	public FormPropertyDefinition clone() {
		ListPropertyDefinition clone = new ListPropertyDefinition();
		clone.setValues(this);
	  return clone;
	}
	
	@Override
	public void setValues(FormPropertyDefinition otherDefinition) {
		if(!(otherDefinition instanceof AlfrescoTransitionsPropertyDefinition)) {
			throw new SimpleWorkflowException("An instance of AlfrescoTransitionProperty is required to set values");
		}
		
		AlfrescoTransitionsPropertyDefinition propDef = (AlfrescoTransitionsPropertyDefinition) otherDefinition;
		setName(propDef.getName());
		setMandatory(propDef.isMandatory());
		setWritable(propDef.isWritable());
		
		// Copy the entries from the other definition
		if(entries == null) {
			entries = new ArrayList<ListPropertyEntry>();
		} else {
			entries.clear();
		}
		
		if(propDef.getTransitions() != null) {
			ListPropertyEntry newEntry = null;
			for(ListPropertyEntry entry : propDef.getTransitions()) {
				newEntry = new ListPropertyEntry(entry.getValue(), entry.getName());
				entries.add(newEntry);
			}
		}
		
		if(otherDefinition.getParameters() != null) {
			setParameters(new HashMap<String, Object>(otherDefinition.getParameters()));
		}
	}

}
