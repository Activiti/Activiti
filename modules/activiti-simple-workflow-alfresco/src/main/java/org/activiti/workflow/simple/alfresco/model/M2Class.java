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
@XmlType(propOrder= {
		"name", "title", "description", "parentName", "properties", "associations", "propertyOverrides",
		"mandatoryAspects"
})
public class M2Class {
	@XmlAttribute
	private String name;
	
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String title;
	
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String description;
	
	@XmlElement(name="parent", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String parentName;
	
	@XmlElementWrapper(name="properties", namespace="http://www.alfresco.org/model/dictionary/1.0")
	@XmlElement(name="property", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private List<M2Property> properties;
	
	@XmlElementWrapper(name="overrides", namespace="http://www.alfresco.org/model/dictionary/1.0")
	@XmlElement(name="property", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private List<M2PropertyOverride> propertyOverrides;
	
	@XmlElementWrapper(name="associations", namespace="http://www.alfresco.org/model/dictionary/1.0")
	@XmlElement(name="association", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private List<M2ClassAssociation> associations;
	
	@XmlElementWrapper(name="mandatory-aspects", namespace="http://www.alfresco.org/model/dictionary/1.0")
	@XmlElement(name="aspect", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private List<String> mandatoryAspects;
	
	public List<String> getMandatoryAspects() {
		ensureMandatoryAspectsInitialized();
	  return mandatoryAspects;
  }
	
	public void setMandatoryAspects(List<String> mandatoryAspects) {
	  this.mandatoryAspects = mandatoryAspects;
  }
	
	public List<M2ClassAssociation> getAssociations() {
		ensureAssociationsInitialized();
	  return associations;
  }
	
	public void setAssociations(List<M2ClassAssociation> associations) {
	  this.associations = associations;
  }
	
	public List<M2Property> getProperties() {
		ensurePropertiesInitialized();
	  return properties;
  }
	
	public void setProperties(List<M2Property> properties) {
	  this.properties = properties;
  }
	
	public List<M2PropertyOverride> getPropertyOverrides() {
		ensurePropertyOverridesInitialized();
	  return propertyOverrides;
  }
	
	public M2PropertyOverride getPropertyOverride(String propertyName) {
		M2PropertyOverride found = null;
		if(propertyOverrides != null) {
			for(M2PropertyOverride override : propertyOverrides) {
				if(propertyName.equals(override.getName())) {
					return override;
				}
			}
		}
		return found;
	}
	
	public void setPropertyOverrides(List<M2PropertyOverride> propertyOverrides) {
	  this.propertyOverrides = propertyOverrides;
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

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	
	private void ensurePropertiesInitialized() {
		if(properties == null) {
			properties = new ArrayList<M2Property>();
		}
  }
	
	private void ensurePropertyOverridesInitialized() {
		if(propertyOverrides == null) {
			propertyOverrides = new ArrayList<M2PropertyOverride>();
		}
  }
	
	private void ensureAssociationsInitialized() {
		if(associations == null) {
			associations = new ArrayList<M2ClassAssociation>();
		}
  }
	
	private void ensureMandatoryAspectsInitialized() {
		if(mandatoryAspects == null) {
			mandatoryAspects = new ArrayList<String>();
		}
  }

}
