package org.activiti.rest.api.identity;

import org.activiti.engine.identity.User;

public class UserResponse {
  
  String id;
  String firstName;
  String lastName;
  String email;
  
  public UserResponse(User user) {
    setId(user.getId());
    setEmail(user.getEmail());
    setFirstName(user.getFirstName());
    setLastName(user.getLastName());
  }
  
  public String getId() {
    return id;
  }
  public UserResponse setId(String id) {
    this.id = id;
    return this;
  }
  public String getFirstName() {
    return firstName;
  }
  public UserResponse setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }
  public String getLastName() {
    return lastName;
  }
  public UserResponse setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }
  public String getEmail() {
    return email;
  }
  public UserResponse setEmail(String email) {
    this.email = email;
    return this;
  }
}
