package org.activiti.cycle.impl.conf;

import java.util.Properties;

import org.activiti.cycle.RepositoryConnector;

/**
 * 
 * @author christian.lipphardt
 */
public abstract class RepositoryConnectorConfiguration {

  /**
   * name (unique!), used for URL and identification
   * 
   * TODO: Use this default? Hmm....
   */
  private String name = this.getClass().getSimpleName().replace("Configuration", "");

  // /**
  // * id (unique!)
  // * short name used in URL to identify repository
  // */
  // private String shortName =
  // this.getClass().getSimpleName().replace("Configuration", "");

  private String description;
  
  /**
   * Name of configuration scope.
   * 
   * Can be - Global - Department X - User Y
   */
  private String configurationScope;

  /**
   * TODO: Decide if we want to keep that here
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

  // @Override
  // public String toString() {
  // return "RepositoryConnectorConfiguration '" +
  // this.getClass().getSimpleName() + "' [getBasePath()=" + getBasePath() +
  // ", getPassword()=" + getPassword()
  // + ", getUser()=" + getUser() + "]";
  // }
}
