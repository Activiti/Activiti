package org.activiti.rest.api.cycle.dto;

public class UrlActionDto {

  private String actionId;

  private String url;

  public UrlActionDto(String id, String url2) {
    this.actionId = id;
    this.url = url2;
  }

  public String getActionId() {
    return actionId;
  }

  public void setActionId(String actionId) {
    this.actionId = actionId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
