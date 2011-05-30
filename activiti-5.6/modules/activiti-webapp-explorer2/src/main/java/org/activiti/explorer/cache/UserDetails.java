package org.activiti.explorer.cache;
public class UserDetails {
    
    protected String userId;
    protected String fullName;
    
    public UserDetails(String userId, String fullName) {
      this.userId = userId;
      this.fullName = fullName;
    }

    public String getUserId() {
      return userId;
    }
    public void setUserId(String userId) {
      this.userId = userId;
    }
    public String getFullName() {
      return fullName;
    }
    public void setFullName(String fullName) {
      this.fullName = fullName;
    }
    
  }