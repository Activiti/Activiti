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

@XmlAccessorType(XmlAccessType.PROPERTY)
public class Bean {
	
	private String id;
	private String parent;
	private List<BeanProperty> properties;
	private String beanClass;
	private String initMethod;
	
	public Bean() {
		
  }
	
	public Bean(String id, String parent) {
	  this.id = id;
	  this.parent = parent;
  }
	
	
	@XmlElement(name="property", namespace="http://www.springframework.org/schema/beans")
	public List<BeanProperty> getProperties() {
		if(properties == null) {
			properties = new ArrayList<BeanProperty>();
		}
	  return properties;
  }
	
	public void setProperties(List<BeanProperty> properties) {
	  this.properties = properties;
  }
	
	@XmlAttribute
	public String getParent() {
	  return parent;
  }
	
	public void setParent(String parent) {
	  this.parent = parent;
  }
	
	@XmlAttribute
	public String getId() {
	  return id;
	}
	  
	public void setId(String id) {
	  this.id = id;
  }
	
	@XmlAttribute(name="class")
	public String getBeanClass() {
	  return beanClass;
  }
	
	public void setBeanClass(String beanClass) {
	  this.beanClass = beanClass;
  }
	
	@XmlAttribute(name="init-method")
	public String getInitMethod() {
	  return initMethod;
  }
	
	public void setInitMethod(String initMethod) {
	  this.initMethod = initMethod;
  }
	
	
}
