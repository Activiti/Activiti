package org.activiti.cycle.impl.conf;

public abstract class PasswordEnabledRepositoryConnectorConfiguration extends RepositoryConnectorConfiguration {
  
  private boolean credentialsSaved = false;

  private String password;

  private String user;
  
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
    this.credentialsSaved = (user != null);
  }

  /**
   * returns if user credentials are saved in this configuration. Otherwise they
   * must be queried when the user first accessed the repo
   */
  public boolean isCredentialsSaved() {
    return credentialsSaved;
  }

}
