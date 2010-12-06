package org.activiti.cycle.incubator.connector.svn;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.conf.PasswordEnabledRepositoryConnectorConfiguration;

/**
 * The Configuration for {@link SvnRepositoryConnector}s
 * 
 * @author daniel.meyer@camunda.com
 * 
 */
public class SvnConnectorConfiguration extends PasswordEnabledRepositoryConnectorConfiguration {

  private String repositoryPath = "";

  private String temporaryFileStore = "";

  public SvnConnectorConfiguration() {
  }

  public SvnConnectorConfiguration(String name, String repoLocation, String tempFiles) {
    repositoryPath = repoLocation;
    setName(name);
    setTemporaryFileStore(tempFiles);
  }

  /**
   * creates and configures a new {@link SvnRepositoryConnector}
   * 
   * @return
   */
  public RepositoryConnector createConnector() {
    RepositoryConnector theConnector =  CycleComponentFactory.getCycleComponentInstance(SvnRepositoryConnector.class, RepositoryConnector.class);
    theConnector.setConfiguration(this);
    return theConnector;
  }

  public String getRepositoryPath() {
    return repositoryPath;
  }

  public void setRepositoryPath(String repositoryPath) {
    this.repositoryPath = repositoryPath;
  }

  public String getTemporaryFileStore() {
    return temporaryFileStore;
  }

  public void setTemporaryFileStore(String temporaryFileStore) {
    this.temporaryFileStore = temporaryFileStore;
  }

  public ArtifactType getDefaultArtifactType() {
    return SvnConnectorPluginDefinition.artifactTypeDefault;
  }

}
