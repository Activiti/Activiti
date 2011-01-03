package org.activiti.cycle.impl.conf;

import java.util.Properties;

import org.activiti.cycle.RepositoryConnector;

/**
 * 
 */
public abstract class RepositoryConnectorConfiguration {

  /**
   * name (unique!), used for URL and identification
   * 
   * TODO: Use this default? Hmm....
   */
  private String id = this.getClass().getSimpleName().replace("Configuration", "");

  private String name = this.getClass().getSimpleName().replace("Configuration", "");

  private String description;

  /**
   * help text to present the user when login fails.
   */
  private String loginHelp;

  /**
   * Name of configuration scope.
   * 
   * Can be - Global - Department X - User Y
   */
  private String configurationScope;

  /**
   * TODO: Decide if we want to keep that here
   * 
   * @param cycleService
   */
  public abstract RepositoryConnector createConnector();

  public Properties getProperties() {
    Properties properties = new Properties();
    resolveProperties(this.getClass(), properties);
    return properties;
  }

  private void resolveProperties(Class clazz, Properties properties) {
    if (clazz == RepositoryConnectorConfiguration.class) {
      // stop recursion in this class
      return;
    }
    // TODO: Add Bean-Access via reflection (JavaBean stuff)

    resolveProperties(clazz.getSuperclass(), properties);
  }

  public void setProperties(Properties properties) {
    // TODO: Change properties via reflection
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLoginHelp() {
    return loginHelp;
  }

  public void setLoginHelp(String loginHelp) {
    this.loginHelp = loginHelp;
  }

  // @Override
  // public String toString() {
  // return "RepositoryConnectorConfiguration '" +
  // this.getClass().getSimpleName() + "' [getBasePath()=" + getBasePath() +
  // ", getPassword()=" + getPassword()
  // + ", getUser()=" + getUser() + "]";
  // }
}
