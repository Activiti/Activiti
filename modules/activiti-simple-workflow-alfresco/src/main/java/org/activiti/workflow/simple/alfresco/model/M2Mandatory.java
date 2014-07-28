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
package org.activiti.workflow.simple.alfresco.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://www.alfresco.org/model/dictionary/1.0")
public class M2Mandatory {
	@XmlAttribute(name="enforced")
	private Boolean enforced;
	
	@XmlValue
	private boolean mandatory = false;
	
	public M2Mandatory() {
		
  }
	
	public M2Mandatory(boolean mandatory) {
		this.mandatory = mandatory;
  }
	
	public boolean isMandatory() {
	  return mandatory;
  }
	
	public Boolean isEnforced() {
	  return enforced;
  }
	
	public void setEnforced(boolean enforced) {
	  this.enforced = enforced;
  }
	
	public void setMandatory(boolean mandatory) {
	  this.mandatory = mandatory;
  }
}
