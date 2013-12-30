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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="model", namespace="http://www.alfresco.org/model/dictionary/1.0")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
	propOrder={
	"name", "description", "author", "version", "imports", "namespaces", "constraints", "types",
	"aspects"
})
public class M2Model {

	@XmlAttribute
	private String name = null;
	
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String description = null;
	
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String author = null;
	
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")	
	private String version;

	@XmlElementWrapper(name="namespaces", namespace="http://www.alfresco.org/model/dictionary/1.0")
	@XmlElement(name="namespace", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private List<M2Namespace> namespaces;
	
	@XmlElementWrapper(name="imports", namespace="http://www.alfresco.org/model/dictionary/1.0")
	@XmlElement(name="import", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private List<M2Namespace> imports;
	
	@XmlElementWrapper(name="types", namespace="http://www.alfresco.org/model/dictionary/1.0")
	@XmlElement(name="type", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private List<M2Type> types;
	
	@XmlElementWrapper(name="aspects", namespace="http://www.alfresco.org/model/dictionary/1.0")
	@XmlElement(name="aspect", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private List<M2Aspect> aspects;
	
	@XmlElementWrapper(name="constraints", namespace="http://www.alfresco.org/model/dictionary/1.0")
	@XmlElement(name="constraint", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private List<M2Constraint> constraints;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<M2Namespace> getNamespaces() {
		ensureNamespacesInitialized();
		return namespaces;
	}

	public void setNamespaces(List<M2Namespace> namespaces) {
		this.namespaces = namespaces;
	}

	public List<M2Namespace> getImports() {
		ensureImportsInitialized();
		return imports;
	}

	public void setImports(List<M2Namespace> imports) {
		this.imports = imports;
	}

	public List<M2Type> getTypes() {
		ensureTypesInitialized();
		return types;
	}

	public void setTypes(List<M2Type> types) {
		this.types = types;
	}

	public List<M2Aspect> getAspects() {
		ensureAspectsInitialized();
		return aspects;
	}

	public void setAspects(List<M2Aspect> aspects) {
		this.aspects = aspects;
	}

	public List<M2Constraint> getConstraints() {
		ensureConstraintsInitialized();
		return constraints;
	}

	public void setConstraints(List<M2Constraint> constraints) {
		this.constraints = constraints;
	}
	
	public M2Aspect getAspect(String aspectName) {
		if(aspects != null) {
			for(M2Aspect aspect : aspects) {
				if(aspect.getName().equals(aspectName)) {
					return aspect;
				}
			}
		}
		return null;
	}
	
	public M2Type getType(String typeName) {
		if(types != null) {
			for(M2Type type : types) {
				if(type.getName().equals(typeName)) {
					return type;
				}
			}
		}
		return null;
	}
	
	/**
	 * @return true, if a property, aspect or association exists with the given name
	 * in any of the types present in this model.
	 */
	public boolean isContainedInModel(String qualifiedName) {
		boolean found = getAspect(qualifiedName) != null;
		
		if(!found) {
			if(getTypes() != null) {
				for(M2Type type : getTypes()) {
					if(found) {
						break;
					}
					
					if(type.getProperties() != null) {
						for(M2Property prop : type.getProperties()) {
							if(qualifiedName.equals(prop.getName())) {
								found = true;
								break;
							}
						}
						
						for(M2ClassAssociation assoc : type.getAssociations()) {
							if(qualifiedName.equals(assoc.getName())) {
								found = true;
								break;
							}
						}
					}
				}
			}
		}
		return found;
	}
	
	private void ensureNamespacesInitialized() {
		if(namespaces == null) {
			namespaces = new ArrayList<M2Namespace>();
		}
  }
	
	private void ensureImportsInitialized() {
		if(imports == null) {
			imports = new ArrayList<M2Namespace>();
		}
	}
	
	private void ensureConstraintsInitialized() {
		if(constraints == null) {
			constraints = new ArrayList<M2Constraint>();
		}
	}
	
	private void ensureTypesInitialized() {
		if(types == null) {
			types = new ArrayList<M2Type>();
		}
	}
	
	private void ensureAspectsInitialized() {
		if(aspects == null) {
			aspects = new ArrayList<M2Aspect>();
		}
	}
}
