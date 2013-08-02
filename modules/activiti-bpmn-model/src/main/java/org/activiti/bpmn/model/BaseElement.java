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
package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class BaseElement {
  
  protected String id;
  protected int xmlRowNumber;
  protected int xmlColumnNumber;
  protected Map<String, List<ExtensionElement>> extensionElements = new LinkedHashMap<String, List<ExtensionElement>>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getXmlRowNumber() {
    return xmlRowNumber;
  }

  public void setXmlRowNumber(int xmlRowNumber) {
    this.xmlRowNumber = xmlRowNumber;
  }

  public int getXmlColumnNumber() {
    return xmlColumnNumber;
  }

  public void setXmlColumnNumber(int xmlColumnNumber) {
    this.xmlColumnNumber = xmlColumnNumber;
  }

  public Map<String, List<ExtensionElement>> getExtensionElements() {
    return extensionElements;
  }
  
  public void addExtensionElement(ExtensionElement extensionElement) {
    if (extensionElement != null && StringUtils.isNotEmpty(extensionElement.getName())) {
      List<ExtensionElement> elementList = null;
      if (this.extensionElements.containsKey(extensionElement.getName()) == false) {
        elementList = new ArrayList<ExtensionElement>();
        this.extensionElements.put(extensionElement.getName(), elementList);
      }
      this.extensionElements.get(extensionElement.getName()).add(extensionElement);
    }
  }

  public void setExtensionElements(Map<String, List<ExtensionElement>> extensionElements) {
    this.extensionElements = extensionElements;
  }
}
