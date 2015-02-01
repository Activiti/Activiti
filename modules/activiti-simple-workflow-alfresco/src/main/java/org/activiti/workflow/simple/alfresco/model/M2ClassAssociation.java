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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://www.alfresco.org/model/dictionary/1.0", 
	propOrder = { "name", "isProtected", "title", "description", "source", "target" })
public class M2ClassAssociation {

	@XmlAttribute
	private String name;

	@XmlAttribute(name = "protected")
	private Boolean isProtected;
	
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String title;
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String description;
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private M2AssociationSource source;
	@XmlElement(namespace="http://www.alfresco.org/model/dictionary/1.0")
	private M2AssociationTarget target;

	public M2AssociationSource getSource() {
		return source;
	}

	public void setSource(M2AssociationSource source) {
		this.source = source;
	}

	public M2AssociationTarget getTarget() {
		return target;
	}

	public void setTarget(M2AssociationTarget target) {
		this.target = target;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getIsProtected() {
		return isProtected;
	}

	public void setIsProtected(Boolean isProtected) {
		this.isProtected = isProtected;
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
}
