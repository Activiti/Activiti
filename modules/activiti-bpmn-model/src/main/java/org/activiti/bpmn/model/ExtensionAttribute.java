package org.activiti.bpmn.model;


public class ExtensionAttribute {

  protected String name;
  protected String value;
  protected String namespacePrefix;
  protected String namespace;
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
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
}
