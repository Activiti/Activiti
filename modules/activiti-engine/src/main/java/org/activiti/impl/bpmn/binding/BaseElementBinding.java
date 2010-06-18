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
package org.activiti.impl.bpmn.binding;

import org.activiti.impl.bpmn.parser.BpmnParse;
import org.activiti.impl.xml.Element;


/**
 * represents the bindings that are common to all BPMN elements.
 * 
 * Corresponds with the baseElement definition in the BPMN 2.0 specification.
 * 
 * @author Joram Barrez
 */
public abstract class BaseElementBinding implements BpmnBinding {
  
  private static final String DOCUMENTATION = "documentation";
  
  public boolean matches(Element element, BpmnParse bpmnParse) {
    return getTagName().equals(element.getTagName());
  }
  
  // Currently only one documentation element is supported (spec says there should be multiple)
  public String parseDocumentation(Element element) {
    Element docElement = element.element(DOCUMENTATION);
    if (docElement != null) {
      return docElement.getText().trim();
    }
    return null;
  }
  
  protected abstract String getTagName();

}
