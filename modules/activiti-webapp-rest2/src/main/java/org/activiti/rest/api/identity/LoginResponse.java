package org.activiti.rest.api.identity;

public class LoginResponse {

  private boolean success;

  public boolean isSuccess() {
    return success;
  }

  public LoginResponse setSuccess(boolean success) {
    this.success = success;
    return this;
  }
}
