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
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://www.alfresco.org/model/dictionary/1.0")
public class M2Constraint {

	@XmlAttribute
	private String name;
	
	@XmlAttribute
	private String ref;
	
	@XmlAttribute
	private String type;
	
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String title;
	
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String description;
	
	@XmlElement(name="parameter", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private List<M2NamedValue> parameters;
	
	public List<M2NamedValue> getParameters() {
		ensureParametersInitialized();
	  return parameters;
  }
	
	public void setParameters(List<M2NamedValue> parameters) {
	  this.parameters = parameters;
  }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
	
	private void ensureParametersInitialized() {
		if(parameters == null) {
			parameters = new ArrayList<M2NamedValue>();
		}
	}

}
