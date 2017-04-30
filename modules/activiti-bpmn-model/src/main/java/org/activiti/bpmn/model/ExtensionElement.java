package org.activiti.bpmn.model;

import java.util.*;

public class ExtensionElement extends BaseElement implements Extension{

  protected String name;
  protected String namespacePrefix;
  protected String namespace;
  protected String elementText;
  protected Map<String, List<ExtensionElement>> childElements = new LinkedHashMap<String, List<ExtensionElement>>();

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
  public Map<String, List<ExtensionElement>> getChildElements() {
    return childElements;
  }
  public void addChildElement(ExtensionElement childElement) {
    if (isInvalidExtension(childElement)) {
      return;
    }
    addKeyToMapIfNotExists(this.childElements, childElement);
    this.childElements.get(childElement.getName()).add(childElement);
  }
  
  public ExtensionElement clone() {
    ExtensionElement clone = new ExtensionElement();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(ExtensionElement otherElement) {
    setName(otherElement.getName());
    setNamespacePrefix(otherElement.getNamespacePrefix());
    setNamespace(otherElement.getNamespace());
    setElementText(otherElement.getElementText());
    setAttributes(cloneMapAttributes(otherElement.getAttributes()));
    childElements = clonedMapWithoutEmptyKeys(otherElement.getChildElements());
  }

  private Map<String,List<ExtensionAttribute>> cloneMapAttributes(Map<String, List<ExtensionAttribute>> attributes) {
    Map<String, List<ExtensionAttribute>> clonedMapAttributes =
            new HashMap<String, List<ExtensionAttribute>>(attributes.size());

    for (String key : attributes.keySet()) {
      clonedMapAttributes.put(key, getClonedExtensions(attributes.get(key)));
    }
    return clonedMapAttributes;
  }
}
