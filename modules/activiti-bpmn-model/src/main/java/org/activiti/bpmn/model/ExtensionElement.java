package org.activiti.bpmn.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class ExtensionElement {

  protected String name;
  protected String namespacePrefix;
  protected String namespace;
  protected String elementText;
  protected Map<String, ExtensionElement> childElements = new LinkedHashMap<String, ExtensionElement>();
  protected Map<String, ExtensionAttribute> attributes = new LinkedHashMap<String, ExtensionAttribute>();
  
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
  public Map<String, ExtensionElement> getChildElements() {
    return childElements;
  }
  public void addChildElement(ExtensionElement childElement) {
    if (childElement != null && StringUtils.isNotEmpty(childElement.getName())) {
      this.childElements.put(childElement.getName(), childElement);
    }
  }
  public void setChildElements(Map<String, ExtensionElement> childElements) {
    this.childElements = childElements;
  }
  public Map<String, ExtensionAttribute> getAttributes() {
    return attributes;
  }
  public void addAttribute(ExtensionAttribute attribute) {
    if (attribute != null && StringUtils.isNotEmpty(attribute.getName())) {
      this.attributes.put(attribute.getName(), attribute);
    }
  }
  public void setAttributes(Map<String, ExtensionAttribute> attributes) {
    this.attributes = attributes;
  }
}
