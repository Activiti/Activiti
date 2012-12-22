package org.activiti.bpmn.model;

public class Import extends BaseElement {

  protected String importType;
  protected String location;
  protected String namespace;
  
  public String getImportType() {
    return importType;
  }
  public void setImportType(String importType) {
    this.importType = importType;
  }
  public String getLocation() {
    return location;
  }
  public void setLocation(String location) {
    this.location = location;
  }
  public String getNamespace() {
    return namespace;
  }
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }
}
