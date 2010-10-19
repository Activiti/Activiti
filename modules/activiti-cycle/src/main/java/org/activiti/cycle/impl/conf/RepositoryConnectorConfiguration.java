package org.activiti.cycle.impl.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.plugin.ActivitiCyclePluginDefinition;

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
  
  private static Map<Class< ? extends RepositoryConnectorConfiguration>, List<ArtifactType>> registeredArtifactTypesPerConnector = new HashMap<Class< ? extends RepositoryConnectorConfiguration>, List<ArtifactType>>();  

  /**
   * TODO: Decide if we want to keep that here
   */
  public abstract RepositoryConnector createConnector();
    
  public static void addPluginDefinition(ActivitiCyclePluginDefinition definition) {
    List<ArtifactType> registeredArtifactTypes = new ArrayList<ArtifactType>();
    definition.addArtifactTypes(registeredArtifactTypes);
    registeredArtifactTypesPerConnector.put(definition.getRepositoryConnectorConfigurationType(), registeredArtifactTypes);
  }
  
  public List<ArtifactType> getArtifactTypes() {
    return getArtifactType(this.getClass());
  }
  
  private List<ArtifactType> getArtifactType(Class confClass) {
    List<ArtifactType> list = registeredArtifactTypesPerConnector.get(confClass);
    if (list == null && RepositoryConnectorConfiguration.class.isAssignableFrom(confClass.getSuperclass())) {
      return getArtifactType(confClass.getSuperclass());
    } else if (list == null) {
      return new ArrayList<ArtifactType>();
    } else {
      return list;
    }
  }
  
  public boolean hasArtifactType(String id) {
    for (ArtifactType type : getArtifactTypes()) {
      if (type.getId().equals(id)) {
        return true;
      }
    }
    return false;
  }
  
  public ArtifactType getArtifactType(String id) {
    for (ArtifactType type : getArtifactTypes()) {
      if (type.getId().equals(id)) {
        return type;
      }
    }
    // check if we have a default
    if (getDefaultArtifactType() != null) {
      return getDefaultArtifactType();
    }
    // otherwise throw exception
    throw new RepositoryException("ArtifactType with id '" + id + "' doesn't exist. Possible types: " + getArtifactTypes());
  }

  /**
   * TODO: THink about that a bit more when refactoring the ARtifactTypes /
   * MimeTypes
   * 
   * Overwrite to set defaulot ArtifactType
   */
  public ArtifactType getDefaultArtifactType() {
    return null;
  }

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

  // @Override
  // public String toString() {
  // return "RepositoryConnectorConfiguration '" +
  // this.getClass().getSimpleName() + "' [getBasePath()=" + getBasePath() +
  // ", getPassword()=" + getPassword()
  // + ", getUser()=" + getUser() + "]";
  // }
}
