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

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Tijs Rademakers
 */
public abstract class BaseElement implements HasExtensionAttributes {

  protected String id;
  protected int xmlRowNumber;
  protected int xmlColumnNumber;
  protected Map<String, List<ExtensionElement>> extensionElements = new LinkedHashMap<String, List<ExtensionElement>>();
  /** extension attributes could be part of each element */
  protected Map<String, List<ExtensionAttribute>> attributes = new LinkedHashMap<String, List<ExtensionAttribute>>();

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
    if (isInvalidExtension(extensionElement)) {
      return;
    }
    addKeyToMapIfNotExists(this.extensionElements, extensionElement);
    this.extensionElements.get(extensionElement.getName()).add(extensionElement);
  }

  public void setExtensionElements(Map<String, List<ExtensionElement>> extensionElements) {
    this.extensionElements = extensionElements;
  }

  @Override
  public Map<String, List<ExtensionAttribute>> getAttributes() {
    return attributes;
  }

  @Override
  public String getAttributeValue(String namespace, String name) {
    List<ExtensionAttribute> attributes = getAttributes().get(name);
    ExtensionAttribute attribute = findFirstAttributeByNamespace(attributes, namespace);
    return attribute!=null ? attribute.getValue() : null;
  }

  private ExtensionAttribute findFirstAttributeByNamespace(List<ExtensionAttribute> attributes, String namespace) {
    if (attributes == null || attributes.isEmpty()) {
      return null;
    }
    for (ExtensionAttribute attribute : attributes) {
      if (isSoughtAttribute(namespace, attribute)){
        return attribute;
      }
    }
    return null;
  }

  private boolean isSoughtAttribute(String namespace, ExtensionAttribute attribute) {
    return (namespace == null && attribute.getNamespace() == null)
            || (namespace!=null && namespace.equals(attribute.getNamespace()));
  }

  @Override
  public void addAttribute(ExtensionAttribute attribute) {
    if (isInvalidExtension(attribute)) {
      return;
    }
    addKeyToMapIfNotExists(this.attributes, attribute);
    this.attributes.get(attribute.getName()).add(attribute);
  }

  protected boolean isInvalidExtension(Extension extension) {
    return extension == null || isEmpty(extension.getName());
  }

  protected  <T extends Extension> void addKeyToMapIfNotExists(Map<String, List<T>> map, Extension extension) {
    if (!map.containsKey(extension.getName())) {
      map.put(extension.getName(),new ArrayList<T>());
    }
  }

  @Override
  public void setAttributes(Map<String, List<ExtensionAttribute>> attributes) {
    this.attributes = attributes;
  }

  public void setValues(BaseElement otherElement) {
    setId(otherElement.getId());
    extensionElements = clonedMapWithoutEmptyKeys(otherElement.getExtensionElements());
    attributes = clonedMapWithoutEmptyKeys(otherElement.getAttributes());
  }


  protected <T extends Extension> LinkedHashMap<String, List<T>> clonedMapWithoutEmptyKeys(Map<String, List<T>> mapExtensions) {
    LinkedHashMap<String, List<T>> map = new LinkedHashMap<String, List<T>>();
    for (String key : mapExtensions.keySet()) {
      List<T> childreExtensionElements = getClonedExtensions(mapExtensions.get(key));
      if (!childreExtensionElements.isEmpty()) {
        map.put(key, childreExtensionElements);
      }
    }
    return map;
  }

  protected  <T extends Extension> List<T> getClonedExtensions(List<T> extensions) {
    if (extensions == null || extensions.isEmpty()) {
      return Collections.emptyList();
    }
    List<T> clonedExtensions = new ArrayList<T>();
    for (T extension : extensions) {
      T cloned = extension.clone();
      clonedExtensions.add(cloned);
    }
    return clonedExtensions;
  }

  public abstract BaseElement clone();
}