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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "className", "role", "mandatory", "many" }, namespace="http://www.alfresco.org/model/dictionary/1.0")
public class M2AssociationTarget {
	@XmlElement(name = "class", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String className;

	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String role;
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private boolean mandatory = false;
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private boolean many = false;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isMany() {
		return many;
	}

	public void setMany(boolean many) {
		this.many = many;
	}
}
