package org.activiti.cycle.impl.connector.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentationDefinition;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.impl.connector.AbstractRepositoryConnector;

/**
 * Connector to represent customized view for a user of cycle to hide all the
 * internal configuration and {@link RepositoryConnector} stuff from the client
 * (e.g. the webapp)
 * 
 * @author bernd.ruecker@camunda.com
 */
public class CustomizedViewConnector extends AbstractRepositoryConnector<CustomizedViewConfiguration> {

  private List<RepositoryConnector> repositoryConnectors;
  private Map<String, RepositoryConnector> repositoryConnectorMap;

  public CustomizedViewConnector(CustomizedViewConfiguration customizedViewConfiguration) {
    super(customizedViewConfiguration);
  }

  /**
   * Get a map with all {@link RepositoryConnector}s created lazily and the name
   * of the connector as key for the map.
   */
  private List<RepositoryConnector> getRepositoryConnectors() {
    if (repositoryConnectors == null) {
      repositoryConnectors = getConfiguration().getConfigurationContainer().getConnectorList();
    }
    return repositoryConnectors;
  }


  private RepositoryConnector getRepositoryConnector(String name) {
    for (RepositoryConnector connector : getRepositoryConnectors()) {
      if (connector.getConfiguration().getName().equals(name)) {
        return connector;
      }
    }
    throw new RepositoryException("Couldn't find Repository Connector with name '" + name + "'");
  }
  
  /**
   * login into all repositories configured (if no username and password was
   * provided by the configuration already).
   * 
   * TODO: Make more sophisticated. Questions: What to do if one repo cannot
   * login?
   */
  public boolean login(String username, String password) {
    for (RepositoryConnector connector : getRepositoryConnectors()) {
      connector.login(username, password);
    }
    return true;
  }

  /**
   * commit pending changes in all repository connectors configured
   */
  public void commitPendingChanges(String comment) {
    for (RepositoryConnector connector : getRepositoryConnectors()) {
      connector.commitPendingChanges(comment);
    }
  }

  /**
   * get the client URL added to all {@link RepositoryNode}s for the client
   * (based on the configured base url)
   */
  private String getClientUrl(RepositoryNode repositoryNode) {
    return getConfiguration().getBaseUrl() + repositoryNode.getId(); 
  }

  /**
   * construct a unique id for an {@link RepositoryNode} by adding the connector
   * name (since this connector maintains different repos)
   */
  private String getIdWithRepoName(RepositoryNode repositoryNode) {
    String repositoryName = repositoryNode.getConnector().getConfiguration().getName();
    if (!repositoryNode.getId().startsWith("/")) {
      throw new RepositoryException("RepositoryNode id doesn't start with a slash, which is copnsidered invalid: '" + repositoryNode.getId()
              + "' in repository '" + repositoryName + "'");
    } else {
      return getRepositoryPrefix(repositoryName) + repositoryNode.getId();
    }
  }

  /**
   * return the prefix for the {@link RepositoryConnector}
   */
  private String getRepositoryPrefix(String repositoryName) {
    return "/" + repositoryName;
  }

  /**
   * create client URL for a {@link ContentRepresentationDefinition}
   */
  private String getClientUrl(RepositoryArtifact artifact, ContentRepresentationDefinition contentRepresentationDefinition) {
    return artifact.getClientUrl() + "/content/" + contentRepresentationDefinition.getName();
  }


  /**
   * add repository name in config to URL
   */
  private RepositoryNode adjust(RepositoryNode repositoryNode) {
    if (repositoryNode instanceof RepositoryArtifact) { 
      RepositoryArtifact artifact = (RepositoryArtifact) repositoryNode;

      // TODO: This is a bit hacky to set the right connector on the actions
      // BEFORE the connector is changed
      // Now is the time to rethink the action generation mechanism!
      artifact.initializeActions();
    }
    
    repositoryNode.setId(getIdWithRepoName(repositoryNode));
    repositoryNode.setClientUrl(getClientUrl(repositoryNode));
    if (repositoryNode instanceof RepositoryArtifact) {       
      RepositoryArtifact artifact = (RepositoryArtifact) repositoryNode;
      
      Collection<ContentRepresentationDefinition> contentRepresentationDefinitions = artifact.getContentRepresentationDefinitions();
      for (ContentRepresentationDefinition contentRepresentationDefinition : contentRepresentationDefinitions) {
        contentRepresentationDefinition.setClientUrl(
                getClientUrl(artifact, contentRepresentationDefinition));
      }
    }

    // and change the connector (last operation to not influence id generating)
    repositoryNode.overwriteConnector(this);

    return repositoryNode;
  }

  protected RepositoryConnector getConnectorFromUrl(String url) {
    RepositoryConnector connector = null;
    
    int index = url.indexOf("/");
    if (index == -1) {
      // demo connector itself
      connector = getRepositoryConnector(url);
    } else if (index == 0) {
      connector = getConnectorFromUrl(url.substring(1));
    } else {
      String repositoryName = url.substring(0, index);
      connector = getRepositoryConnector(repositoryName);
    }
    
    if (connector == null) {
      throw new RepositoryException("Couldn't find any RepositoryConnector for url '" + url + "'");
    } else {
      return connector;
    }
  }

  protected String getRepositoryPartOfUrl(String url) {
    int index = url.indexOf("/");
    if (index == -1) {
      // demo connector itself -> root folder is shown
      return "/";
    } else if (index == 0) {
      return getRepositoryPartOfUrl(url.substring(1));
    } else {
      return url.substring(index);
    }
  }

  public List<RepositoryNode> getChildNodes(String parentUrl) {
    // special handling for root
    if ("/".equals(parentUrl)) {
      return getRepoRootFolders();
    } 
    
    // First identify correct repo and truncate path to local part of
    // connector
    RepositoryConnector connector = getConnectorFromUrl(parentUrl);
    parentUrl = getRepositoryPartOfUrl(parentUrl);

      // now make the query
    List<RepositoryNode> childNodes = connector.getChildNodes(parentUrl);
   
    // and adjust the result to include repo name
    for (RepositoryNode repositoryNode : childNodes) {
       adjust(repositoryNode);
    }
    return childNodes;
  }

  public List<RepositoryNode> getRepoRootFolders() {
    ArrayList<RepositoryNode> nodes = new ArrayList<RepositoryNode>();
    for (RepositoryConnector connector : getRepositoryConnectors()) {
      String repoName = connector.getConfiguration().getName();
      RepositoryFolder folder = new RepositoryFolder(this);
      folder.getMetadata().setName(repoName);
      folder.getMetadata().setPath("/" + repoName);
      folder.setId(repoName);
      folder.setClientUrl(getClientUrl(folder));
      nodes.add(folder);
    }
    return nodes;
  }

  public RepositoryArtifact getRepositoryArtifact(String id) {
    RepositoryArtifact repositoryArtifact = getConnectorFromUrl(id).getRepositoryArtifact(
            getRepositoryPartOfUrl(id));
    adjust(repositoryArtifact);
    return repositoryArtifact;
  }

  public RepositoryFolder getRepositoryFolder(String id) {
    RepositoryFolder repositoryFolder = getConnectorFromUrl(id).getRepositoryFolder(getRepositoryPartOfUrl(id));
    adjust(repositoryFolder);
    return repositoryFolder;
  }

  // public RepositoryNode getRepositoryNode(String id) {
  // RepositoryNode repositoryNode =
  // getConnectorFromUrl(id).getRepositoryNode(getRepositoryPartOfUrl(id));
  // adjust(repositoryNode);
  // return repositoryNode;
  // }

  public void createNewArtifact(String containingFolderId, RepositoryArtifact artifact, Content artifactContent) {
    // TODO: Do we have to change artifact id? I think yes
    // artifact.setId(getRepositoryPartOfUrl(artifact.getId()));
    getConnectorFromUrl(containingFolderId).createNewArtifact(getRepositoryPartOfUrl(containingFolderId), artifact, artifactContent);
  }

  public void modifyArtifact(RepositoryArtifact artifact, ContentRepresentationDefinition artifactContent) {
    RepositoryConnector connector = getConnectorFromUrl(artifact.getId());
    artifact.setId(getRepositoryPartOfUrl(artifact.getId()));    
    connector.modifyArtifact(artifact, artifactContent);
  }

  public void createNewSubFolder(String parentFolderUrl, RepositoryFolder subFolder) {
    // TODO: Do we have to change subFolder id?
    getConnectorFromUrl(parentFolderUrl).createNewSubFolder(getRepositoryPartOfUrl(parentFolderUrl), subFolder);
  }

  public void deleteArtifact(String artifactUrl) {
    getConnectorFromUrl(artifactUrl).deleteArtifact(getRepositoryPartOfUrl(artifactUrl));
  }

  public void deleteSubFolder(String subFolderUrl) {
    getConnectorFromUrl(subFolderUrl).deleteSubFolder(getRepositoryPartOfUrl(subFolderUrl));
  }

  public Content getContent(String nodeId, String representationName) {
    return getConnectorFromUrl(nodeId).getContent(getRepositoryPartOfUrl(nodeId), representationName);
  }

}
