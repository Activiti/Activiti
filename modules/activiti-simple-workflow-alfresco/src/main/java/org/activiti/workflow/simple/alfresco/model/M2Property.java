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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "name", "title", "description", "propertyType", "isProtected", "mandatory", "multiValued",
		"defaultValue", "constraints"})
public class M2Property {

	@XmlAttribute
	private String name;
	
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String title;
	
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String description;

	@XmlElement(name = "type", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String propertyType;

	@XmlElement(name="protected", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private boolean isProtected = false;
	
	@XmlElement(name="multiple", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private boolean multiValued = false;
	
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private M2Mandatory mandatory;
	
	@XmlElement(name="default", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String defaultValue;
	
	@XmlElementWrapper(name="constraints", namespace="http://www.alfresco.org/model/dictionary/1.0")
	@XmlElement(name="constraint", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private List<M2Constraint> constraints;

	public List<M2Constraint> getConstraints() {
		ensureConstraintsInitialized();
		return constraints;
	}

	public void setConstraints(List<M2Constraint> constraints) {
		this.constraints = constraints;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isProtected() {
		return isProtected;
	}

	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}

	public void setMandatory(M2Mandatory mandatory) {
	  this.mandatory = mandatory;
  }
	
	public M2Mandatory getMandatory() {
	  return mandatory;
  }

	public boolean isMultiValued() {
		return multiValued;
	}

	public void setMultiValued(boolean multiValued) {
		this.multiValued = multiValued;
	}

	private void ensureConstraintsInitialized() {
		if (constraints == null) {
			constraints = new ArrayList<M2Constraint>();
		}
	}
}
