package org.activiti.cycle.incubator.connector.vfs;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.conf.PasswordEnabledRepositoryConnectorConfiguration;

/**
 * Configuration class for vfs-based connectors.
 * 
 * Implementation Node: register protocols in {@link #createConnector()}.
 * 
 * @author daniel.meyer@camunda.com
 */
public class VfsConnectorConfiguration extends PasswordEnabledRepositoryConnectorConfiguration {

  private String hostname;

  private String path;

  private String protocol;

  public VfsConnectorConfiguration(String name, String hostname, String path, String protocol) {
    setHostname(hostname);
    setPath(path);
    setName(name);
    setProtocol(protocol);
  }

  public VfsConnectorConfiguration() {

  }

  public RepositoryConnector createConnector() {
    if (protocol.equals("sftp")) {
      RepositoryConnector connectorInstance = CycleComponentFactory.getCycleComponentInstance(SftpRepositoryConnector.class, RepositoryConnector.class);
      connectorInstance.setConfiguration(this);
      return connectorInstance;
    }

    throw new RepositoryException("No connector found for protocol " + protocol);
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }
}
