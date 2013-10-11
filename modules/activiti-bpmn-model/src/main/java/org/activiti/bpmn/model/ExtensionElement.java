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
      if (this.childElements.containsKey(childElement.getName()) == false) {
        elementList = new ArrayList<ExtensionElement>();
        this.childElements.put(childElement.getName(), elementList);
      }
      this.childElements.get(childElement.getName()).add(childElement);
    }
  }
  public void setChildElements(Map<String, List<ExtensionElement>> childElements) {
    this.childElements = childElements;
  }
}
