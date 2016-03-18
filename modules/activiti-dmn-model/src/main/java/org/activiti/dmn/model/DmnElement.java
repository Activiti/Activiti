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
package org.activiti.dmn.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Yvo Swillens
 * @author Bassam Al-Sarori
 */
public abstract class DmnElement {

    protected String id;
    protected String label;
    protected String description;
    protected Map<String, List<DmnExtensionElement>> extensionElements = new LinkedHashMap<String, List<DmnExtensionElement>>();
    /** extension attributes could be part of each element */
    protected Map<String, List<DmnExtensionAttribute>> attributes = new LinkedHashMap<String, List<DmnExtensionAttribute>>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public Map<String, List<DmnExtensionElement>> getExtensionElements() {
        return extensionElements;
    }
    
    public void addExtensionElement(DmnExtensionElement extensionElement) {
        if (extensionElement != null && extensionElement.getName() != null && !extensionElement.getName().trim().isEmpty()) {
          List<DmnExtensionElement> elementList = null;
          if (this.extensionElements.containsKey(extensionElement.getName()) == false) {
            elementList = new ArrayList<DmnExtensionElement>();
            this.extensionElements.put(extensionElement.getName(), elementList);
          }
          this.extensionElements.get(extensionElement.getName()).add(extensionElement);
        }
      }

      public void setExtensionElements(Map<String, List<DmnExtensionElement>> extensionElements) {
        this.extensionElements = extensionElements;
      }


    public Map<String, List<DmnExtensionAttribute>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, List<DmnExtensionAttribute>> attributes) {
        this.attributes = attributes;
    }

    public String getAttributeValue(String namespace, String name) {
        List<DmnExtensionAttribute> attributes = getAttributes().get(name);
        if (attributes != null && !attributes.isEmpty()) {
            for (DmnExtensionAttribute attribute : attributes) {
                if ( (namespace == null && attribute.getNamespace() == null)
                        || namespace.equals(attribute.getNamespace()) )
                    return attribute.getValue();
            }
        }
        return null;
    }

    public void addAttribute(DmnExtensionAttribute attribute) {
        if (attribute != null && attribute.getName() != null && !attribute.getName().trim().isEmpty()) {
            List<DmnExtensionAttribute> attributeList = null;
            if (this.attributes.containsKey(attribute.getName()) == false) {
                attributeList = new ArrayList<DmnExtensionAttribute>();
                this.attributes.put(attribute.getName(), attributeList);
            }
            this.attributes.get(attribute.getName()).add(attribute);
        }
    }

    public void setValues(DmnElement otherElement) {
        setId(otherElement.getId());

        extensionElements = new LinkedHashMap<String, List<DmnExtensionElement>>();
        if (otherElement.getExtensionElements() != null && !otherElement.getExtensionElements().isEmpty()) {
            for (String key : otherElement.getExtensionElements().keySet()) {
                List<DmnExtensionElement> otherElementList = otherElement.getExtensionElements().get(key);
                if (otherElementList != null && !otherElementList.isEmpty()) {
                    List<DmnExtensionElement> elementList = new ArrayList<DmnExtensionElement>();
                    for (DmnExtensionElement extensionElement : otherElementList) {
                        elementList.add(extensionElement.clone());
                    }
                    extensionElements.put(key, elementList);
                }
            }
        }

        attributes = new LinkedHashMap<String, List<DmnExtensionAttribute>>();
        if (otherElement.getAttributes() != null && !otherElement.getAttributes().isEmpty()) {
            for (String key : otherElement.getAttributes().keySet()) {
                List<DmnExtensionAttribute> otherAttributeList = otherElement.getAttributes().get(key);
                if (otherAttributeList != null && !otherAttributeList.isEmpty()) {
                    List<DmnExtensionAttribute> attributeList = new ArrayList<DmnExtensionAttribute>();
                    for (DmnExtensionAttribute extensionAttribute : otherAttributeList) {
                        attributeList.add(extensionAttribute.clone());
                    }
                    attributes.put(key, attributeList);
                }
            }
        }
    }
}
