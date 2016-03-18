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
 * @author Bassam Al-Sarori
 */
public class DmnExtensionElement extends DmnElement {

    protected String name;
    protected String namespacePrefix;
    protected String namespace;
    protected String elementText;
    protected Map<String, List<DmnExtensionElement>> childElements = new LinkedHashMap<String, List<DmnExtensionElement>>();
    /** extension attributes could be part of each element */
    protected Map<String, List<DmnExtensionAttribute>> attributes = new LinkedHashMap<String, List<DmnExtensionAttribute>>();

    public String getElementText() {
        return elementText;
    }
    public void setElementText(String elementText) {
        this.elementText = elementText;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getNamespacePrefix() {
        return namespacePrefix;
    }
    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }
    public String getNamespace() {
        return namespace;
    }
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    public Map<String, List<DmnExtensionElement>> getChildElements() {
        return childElements;
    }
    public void addChildElement(DmnExtensionElement childElement) {
        if (childElement != null && childElement.getName() != null && !childElement.getName().trim().isEmpty()) {
            List<DmnExtensionElement> elementList = null;
            if (this.childElements.containsKey(childElement.getName()) == false) {
                elementList = new ArrayList<DmnExtensionElement>();
                this.childElements.put(childElement.getName(), elementList);
            }
            this.childElements.get(childElement.getName()).add(childElement);
        }
    }
    public void setChildElements(Map<String, List<DmnExtensionElement>> childElements) {
        this.childElements = childElements;
    }

    public DmnExtensionElement clone() {
        DmnExtensionElement clone = new DmnExtensionElement();
        clone.setValues(this);
        return clone;
    }

    public void setValues(DmnExtensionElement otherElement) {
        setName(otherElement.getName());
        setNamespacePrefix(otherElement.getNamespacePrefix());
        setNamespace(otherElement.getNamespace());
        setElementText(otherElement.getElementText());
        setAttributes(otherElement.getAttributes());

        childElements = new LinkedHashMap<String, List<DmnExtensionElement>>();
        if (otherElement.getChildElements() != null && !otherElement.getChildElements().isEmpty()) {
            for (String key : otherElement.getChildElements().keySet()) {
                List<DmnExtensionElement> otherElementList = otherElement.getChildElements().get(key);
                if (otherElementList != null && !otherElementList.isEmpty()) {
                    List<DmnExtensionElement> elementList = new ArrayList<DmnExtensionElement>();
                    for (DmnExtensionElement dmnExtensionElement : otherElementList) {
                        elementList.add(dmnExtensionElement.clone());
                    }
                    childElements.put(key, elementList);
                }
            }
        }
    }
}
