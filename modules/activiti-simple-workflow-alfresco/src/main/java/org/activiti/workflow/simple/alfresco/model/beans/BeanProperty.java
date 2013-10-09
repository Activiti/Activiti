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
package org.activiti.workflow.simple.alfresco.model.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class BeanProperty {

	private String name;
	private List<String> list;
	private List<BeanPropertyProp> props;
	private String ref;
	
	public BeanProperty() {
		
  }
	
	public BeanProperty(String name) {
	  this.name = name;
  }

	@XmlAttribute
	public String getName() {
	  return name;
  }
	
	public void setRef(String ref) {
	  this.ref = ref;
  }
	
	@XmlAttribute
	public String getRef() {
	  return ref;
  }
	
	@XmlElementWrapper(name="list", namespace="http://www.springframework.org/schema/beans")
	@XmlElement(name="value", namespace="http://www.springframework.org/schema/beans")
	public List<String> getList() {
	  return list;
  }
	
	public void addListItem(String item) {
		if(list == null) {
			list = new ArrayList<String>();
		}
		list.add(item);
	}
	
	public void setName(String name) {
	  this.name = name;
  }
	
	@XmlElement(name="prop", namespace="http://www.springframework.org/schema/beans")
	@XmlElementWrapper(name="props", namespace="http://www.springframework.org/schema/beans")
	public List<BeanPropertyProp> getProps() {
	  return props;
  }
	
	public void addProp(BeanPropertyProp prop) {
		if(props ==  null) {
			props = new ArrayList<BeanPropertyProp>();
		}
		props.add(prop);
	}
	
	public void setProps(List<BeanPropertyProp> props) {
	  this.props = props;
  }
}
