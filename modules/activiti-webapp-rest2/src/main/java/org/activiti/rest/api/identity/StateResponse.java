package org.activiti.rest.api.identity;

public class StateResponse {

  boolean success;

  public boolean isSuccess() {
    return success;
  }

  public StateResponse setSuccess(boolean success) {
    this.success = success;
    return this;
  }
}
