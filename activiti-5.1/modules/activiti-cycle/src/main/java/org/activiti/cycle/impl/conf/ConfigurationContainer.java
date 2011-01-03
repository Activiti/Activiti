package org.activiti.cycle.impl.conf;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;

/**
 * rename?
 * 
 * @author ruecker
 */
public class ConfigurationContainer {
  
  private String name;
  
  private List<RepositoryConnectorConfiguration> linkedConnectors = new ArrayList<RepositoryConnectorConfiguration>();

  /**
   * Allow hierarchy for configurations (so you could have enterprise wide
   * Signavio, department wide SVN, team shared folders and special user
   * specific stuff for e.g. local file system
   * 
   * We allowed multiple parent {@link ConfigurationContainer}s to be flexible.
   * Maybe that is not the best idea for complexity?
   */
  private List<ConfigurationContainer> parentContainers = new ArrayList<ConfigurationContainer>();

  // TODO: Add list of Tags and stuff

  public ConfigurationContainer() {
  }

  public ConfigurationContainer(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Get all Connectors for this config including all parents
   */
  public List<RepositoryConnectorConfiguration> getConnectorConfigurations() {
    ArrayList<RepositoryConnectorConfiguration> connectors = new ArrayList<RepositoryConnectorConfiguration>();
    connectors.addAll(linkedConnectors);

    for (ConfigurationContainer parent : getParentContainers()) {
      // TODO: make sure we don't run in an endless loop when we have cyclic
      // linking
      connectors.addAll(parent.getConnectorConfigurations());
    }
    
    return connectors;
  }
  
  // don't provide a map, you loose the ordering!
  // public Map<String, RepositoryConnector> getConnectorMap() {
  // HashMap<String, RepositoryConnector> connectors = new HashMap<String,
  // RepositoryConnector>();
  //    
  // for (RepositoryConnectorConfiguration conf : getConnectorConfigurations())
  // {
  // connectors.put(conf.getName(), conf.createConnector());
  // }
  //
  // return connectors;
  // }

  public List<RepositoryConnector> getConnectorList() {
    List<RepositoryConnector> connectors = new ArrayList<RepositoryConnector>();

    for (RepositoryConnectorConfiguration conf : getConnectorConfigurations()) {
      connectors.add(conf.createConnector());
    }

    return connectors;
  }

  public RepositoryConnector getConnector(String name) {
    for (RepositoryConnectorConfiguration conf : getConnectorConfigurations()) {
      if (conf.getName().equals(name)) {
        return conf.createConnector();
      }
    }
    throw new RepositoryException("Couldn't find Repository Connector Configuration with name '" + name + "'");
  }

  // TODO: keep the stuff locally?
  // private void initConnectors() {
  // HashMap<String, RepositoryConnector> connectors = new HashMap<String,
  // RepositoryConnector>();
  //    
  // for (RepositoryConnectorConfiguration conf : getConnectorConfigurations())
  // {
  // connectors.put(conf.getName(), conf.createConnector());
  // }
  //
  // return connectors;
  // }

  public List<ConfigurationContainer> getParentContainers() {
    return parentContainers;
  }

  public boolean addRepositoryConnectorConfiguration(RepositoryConnectorConfiguration e) {
    return linkedConnectors.add(e);
  }

  public boolean removeRepositoryConnectorConfiguration(RepositoryConnectorConfiguration o) {
    return linkedConnectors.remove(o);
  }

  public boolean addParent(ConfigurationContainer e) {
    return parentContainers.add(e);
  }

  public boolean removeParent(ConfigurationContainer o) {
    return parentContainers.remove(o);
  }
  
}
