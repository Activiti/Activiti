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
/**
 * @author Frederik Heremans
 */
@XmlSchema(
		elementFormDefault=XmlNsForm.QUALIFIED,
		xmlns = {
				@XmlNs(prefix="jos", namespaceURI="http://www.springframework.org/schema/beans"),
				@XmlNs(prefix="tx", namespaceURI="http://www.springframework.org/schema/jee"),
		}
)
package org.activiti.workflow.simple.alfresco.model.beans;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;
