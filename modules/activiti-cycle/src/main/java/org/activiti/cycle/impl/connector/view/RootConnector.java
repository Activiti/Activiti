package org.activiti.cycle.impl.connector.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.RepositoryFolderImpl;
import org.activiti.cycle.impl.RepositoryNodeCollectionImpl;
import org.activiti.cycle.impl.RepositoryNodeImpl;

/**
 * Connector to represent customized view for a user of cycle to hide all the
 * internal configuration and {@link RepositoryConnector} stuff from the client
 * (e.g. the webapp)
 * 
 * @author bernd.ruecker@camunda.com
 */
public class RootConnector implements RepositoryConnector {

  public static final String ROOT_CONNECTOR_ID = "ROOT-CONNECTOR";

  private List<RepositoryConnector> repositoryConnectors;
  
  private RootConnectorConfiguration configuration;

  public RootConnector(RootConnectorConfiguration customizedViewConfiguration) {
    configuration = customizedViewConfiguration;
  }

  public RootConnectorConfiguration getConfiguration() {
    return configuration;
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


  private RepositoryConnector getRepositoryConnector(String connectorId) {
    for (RepositoryConnector connector : getRepositoryConnectors()) {
      if (connector.getConfiguration().getId().equals(connectorId)) {
        return connector;
      }
    }
    throw new RepositoryException("Couldn't find Repository Connector with id '" + connectorId + "'");
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
      // TODO: What if one repository failes? Try loading the other ones and
      // remove the failing from the repo list? Example: Online SIgnavio when
      // offile
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
   * add repository name in config to URL
   */
  private void adjust(RepositoryConnector connector, RepositoryNode node) {
    RepositoryNodeImpl repositoryNode = ((RepositoryNodeImpl) node);
    repositoryNode.addNewRootToCurrentPath(connector.getConfiguration().getId());
  }

  protected RepositoryConnector getConnectorFromUrl(String parentCurrentPath) {
    RepositoryConnector connector = null;
    
    int index = parentCurrentPath.indexOf("/");
    if (index == -1) {
      // demo connector itself
      connector = getRepositoryConnector(parentCurrentPath);
    } else if (index == 0) {
      connector = getConnectorFromUrl(parentCurrentPath.substring(1));
    } else {
      String repositoryName = parentCurrentPath.substring(0, index);
      connector = getRepositoryConnector(repositoryName);
    }
    
    if (connector == null) {
      throw new RepositoryException("Couldn't find any RepositoryConnector for url '" + parentCurrentPath + "'");
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

  public RepositoryNodeCollection getChildren(String parentCurrentPath) {
    // special handling for root
    if ("/".equals(parentCurrentPath)) {
      return getRepoRootFolders();
    } 
    
    // First identify correct repo and truncate path to local part of
    // connector
    RepositoryConnector connector = getConnectorFromUrl(parentCurrentPath);
    parentCurrentPath = getRepositoryPartOfUrl(parentCurrentPath);

      // now make the query
    RepositoryNodeCollection children = connector.getChildren(parentCurrentPath);
   
    // and adjust the result to include repo name
    for (RepositoryNode repositoryNode : children.asList()) {
       adjust(connector, repositoryNode);
    }
    return children;
  }

  public RepositoryNodeCollection getRepoRootFolders() {
    ArrayList<RepositoryNode> nodes = new ArrayList<RepositoryNode>();
    for (RepositoryConnector connector : getRepositoryConnectors()) {

      RepositoryFolderImpl folder = new RepositoryFolderImpl(ROOT_CONNECTOR_ID, connector.getConfiguration().getId());
      folder.getMetadata().setName(connector.getConfiguration().getName());
      folder.getMetadata().setParentFolderId("/");
      nodes.add(folder);
      
    }
    return new RepositoryNodeCollectionImpl(nodes);
  }

  public RepositoryArtifact getRepositoryArtifact(String id) {
    RepositoryConnector connector = getConnectorFromUrl(id);
    RepositoryArtifact repositoryArtifact = connector.getRepositoryArtifact(
            getRepositoryPartOfUrl(id));
    adjust(connector, repositoryArtifact);
    return repositoryArtifact;
  }

  public Content getRepositoryArtifactPreview(String artifactId) throws RepositoryNodeNotFoundException {
    RepositoryConnector connector = getConnectorFromUrl(artifactId);
    return connector.getRepositoryArtifactPreview(getRepositoryPartOfUrl(artifactId));
  }
  
  public RepositoryFolder getRepositoryFolder(String id) {
    RepositoryConnector connector = getConnectorFromUrl(id);
    RepositoryFolder repositoryFolder = connector.getRepositoryFolder(getRepositoryPartOfUrl(id));
    adjust(connector, repositoryFolder);
    return repositoryFolder;
  }
  
  public RepositoryArtifact createArtifact(String containingFolderId, String artifactName, String artifactType, Content artifactContent)
          throws RepositoryNodeNotFoundException {
    return getConnectorFromUrl(containingFolderId).createArtifact(getRepositoryPartOfUrl(containingFolderId), artifactName, artifactType, artifactContent);
  }

  public RepositoryArtifact createArtifactFromContentRepresentation(String containingFolderId, String artifactName, String artifactType,
          String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
    return getConnectorFromUrl(containingFolderId).createArtifactFromContentRepresentation(getRepositoryPartOfUrl(containingFolderId), artifactName,
            artifactType, contentRepresentationName, artifactContent);
  }

  
  public void updateContent(String artifactId, Content content) throws RepositoryNodeNotFoundException {
    RepositoryConnector connector = getConnectorFromUrl(artifactId);
    connector.updateContent(getRepositoryPartOfUrl(artifactId), content);
  }

  public void updateContent(String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException {
    RepositoryConnector connector = getConnectorFromUrl(artifactId);
    connector.updateContent(getRepositoryPartOfUrl(artifactId), contentRepresentationName, content);
  }

  public RepositoryFolder createFolder(String parentFolderId, String name) throws RepositoryNodeNotFoundException {
    return getConnectorFromUrl(parentFolderId).createFolder(getRepositoryPartOfUrl(parentFolderId), name);
  }

  public void deleteArtifact(String artifactUrl) {
    getConnectorFromUrl(artifactUrl).deleteArtifact(getRepositoryPartOfUrl(artifactUrl));
  }

  public void deleteFolder(String subFolderUrl) {
    getConnectorFromUrl(subFolderUrl).deleteFolder(getRepositoryPartOfUrl(subFolderUrl));
  }

  public Content getContent(String artifactId, String representationName) throws RepositoryNodeNotFoundException {
    return getConnectorFromUrl(artifactId).getContent(getRepositoryPartOfUrl(artifactId), representationName);
  }

  public void executeParameterizedAction(String artifactId, String actionId, Map<String, Object> parameters) throws Exception {
    RepositoryConnector connector = getConnectorFromUrl(artifactId);
    String repoPartOfId = getRepositoryPartOfUrl(artifactId);
    for (Entry<String, Object> parameter : new HashSet<Entry<String, Object>>(parameters.entrySet())) {
      if (parameter.getKey().equals("treeTarget")) {
          String targetFolderId = (String) parameter.getValue();
          parameters.put("targetFolderConnector", getConnectorFromUrl(targetFolderId));
          parameters.put("targetFolder", getRepositoryPartOfUrl(targetFolderId));
      }
    }
    connector.executeParameterizedAction(repoPartOfId, actionId, parameters);
  }

  public List<ArtifactType> getSupportedArtifactTypes(String folderId) {
    if (folderId == null || folderId.length() <= 1) {
      // "virtual" root folder doesn't support any artifact types
      return new ArrayList<ArtifactType>();
    }
    return getConnectorFromUrl(folderId).getSupportedArtifactTypes(getRepositoryPartOfUrl(folderId));
  }

}
