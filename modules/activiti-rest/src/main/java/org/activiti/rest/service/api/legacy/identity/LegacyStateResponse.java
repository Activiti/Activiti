package org.activiti.rest.service.api.legacy.identity;

public class LegacyStateResponse {

  boolean success;

  public boolean isSuccess() {
    return success;
  }

  public LegacyStateResponse setSuccess(boolean success) {
    this.success = success;
    return this;
  }
}
