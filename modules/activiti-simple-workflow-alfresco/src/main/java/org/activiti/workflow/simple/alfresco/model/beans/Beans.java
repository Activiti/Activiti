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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "beans", namespace="http://www.springframework.org/schema/beans")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Beans {

	private List<Bean> beans;
	
	@XmlElement(name="bean", namespace="http://www.springframework.org/schema/beans")
	public List<Bean> getBeans() {
		if(beans == null) {
			beans = new ArrayList<Bean>();
		}
	  return beans;
  }
	
	public void setBeans(List<Bean> beans) {
	  this.beans = beans;
  }
	
}
