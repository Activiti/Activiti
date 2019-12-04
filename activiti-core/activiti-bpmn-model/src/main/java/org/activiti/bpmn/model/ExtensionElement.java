package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class ExtensionElement extends BaseElement {

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
    if (childElement != null && StringUtils.isNotEmpty(childElement.getName())) {
      List<ExtensionElement> elementList = null;
      if (!this.childElements.containsKey(childElement.getName())) {
        elementList = new ArrayList<ExtensionElement>();
        this.childElements.put(childElement.getName(), elementList);
      }
      this.childElements.get(childElement.getName()).add(childElement);
    }
  }

  public void setChildElements(Map<String, List<ExtensionElement>> childElements) {
    this.childElements = childElements;
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
    setAttributes(otherElement.getAttributes());

    childElements = new LinkedHashMap<String, List<ExtensionElement>>();
    if (otherElement.getChildElements() != null && !otherElement.getChildElements().isEmpty()) {
      for (String key : otherElement.getChildElements().keySet()) {
        List<ExtensionElement> otherElementList = otherElement.getChildElements().get(key);
        if (otherElementList != null && !otherElementList.isEmpty()) {
          List<ExtensionElement> elementList = new ArrayList<ExtensionElement>();
          for (ExtensionElement extensionElement : otherElementList) {
            elementList.add(extensionElement.clone());
          }
          childElements.put(key, elementList);
        }
      }
    }
  }
}
