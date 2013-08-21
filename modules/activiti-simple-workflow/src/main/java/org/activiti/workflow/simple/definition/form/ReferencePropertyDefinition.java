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

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * A form-property with a value that represents a reference to another entity.
 *  
 * @author Frederik Heremans
 */
@JsonTypeName("reference")
public class ReferencePropertyDefinition extends FormPropertyDefinition {
	
	protected String type;
	
	public void setType(String type) {
	  this.type = type;
  }
	
	/**
	 * @return the type of object that is referenced.
	 */
	public String getType() {
	  return type;
  }
}
