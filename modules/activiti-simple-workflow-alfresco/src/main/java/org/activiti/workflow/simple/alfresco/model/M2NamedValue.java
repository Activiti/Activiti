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

@XmlAccessorType(XmlAccessType.FIELD)
public class M2NamedValue {

	@XmlAttribute
	private String name;
	
	@XmlElement(name="value", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private String simpleValue = null;
	
	@XmlElementWrapper(name="list", namespace="http://www.alfresco.org/model/dictionary/1.0")
	@XmlElement(name="value", namespace="http://www.alfresco.org/model/dictionary/1.0")
	private List<String> listValue = null;

	public M2NamedValue() {
  }
	
	public M2NamedValue(String name, String simpleValue, List<String> listValue) {
	  this.name = name;
	  this.simpleValue = simpleValue;
	  this.listValue = listValue;
  }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSimpleValue() {
		return simpleValue;
	}

	public void setSimpleValue(String simpleValue) {
		this.simpleValue = simpleValue;
	}

	public List<String> getListValue() {
		ensureListValueInitialized();
		return listValue;
	}

	public void setListValue(List<String> listValue) {
		this.listValue = listValue;
	}
	
	private void ensureListValueInitialized() {
		if(listValue == null) {
			listValue = new ArrayList<String>();
		}
  }
}
