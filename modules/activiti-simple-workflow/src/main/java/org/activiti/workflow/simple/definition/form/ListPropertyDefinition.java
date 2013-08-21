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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * A form-property with a value that is represented as single item selected
 * from a list of possible values.
 *  
 * @author Frederik Heremans
 */
@JsonTypeName("list")
public class ListPropertyDefinition extends FormPropertyDefinition {

	protected List<ListPropertyEntry> entries = new ArrayList<ListPropertyEntry>();
	
	public void setEntries(List<ListPropertyEntry> entries) {
	  this.entries = entries;
  }
	
	@JsonSerialize(contentAs=ListPropertyEntry.class)
	public List<ListPropertyEntry> getEntries() {
	  return entries;
  }
	
	public void addEntry(ListPropertyEntry entry) {
		entries.add(entry);
	}
}
