package org.activiti.rest.api.engine;

public class ProcessEngineInfoResponse {

  private String name;
  private String resourceUrl;
  private String exception;
  private String version;
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getResourceUrl() {
    return resourceUrl;
  }
  public void setResourceUrl(String resourceUrl) {
    this.resourceUrl = resourceUrl;
  }
  public String getException() {
    return exception;
  }
  public void setException(String exception) {
    this.exception = exception;
  }
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }
}
