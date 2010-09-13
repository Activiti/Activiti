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
public class CustomizedViewConnector implements RepositoryConnector {

  private List<RepositoryConnector> repositoryConnectors;
  
  private CustomizedViewConfiguration configuration;

  public CustomizedViewConnector(CustomizedViewConfiguration customizedViewConfiguration) {
    configuration = customizedViewConfiguration;
  }

  public CustomizedViewConfiguration getConfiguration() {
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
   * construct a unique id for an {@link RepositoryNode} by adding the connector
   * name (since this connector maintains different repos)
   */
  private String getIdWithRepoName(RepositoryConnector connector, RepositoryNode repositoryNode) {
    String repositoryName = connector.getConfiguration().getName();
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
   * add repository name in config to URL
   */
  private RepositoryNode adjust(RepositoryConnector connector, RepositoryNode node) {
    RepositoryNodeImpl repositoryNode = ((RepositoryNodeImpl) node);
    repositoryNode.setId(getIdWithRepoName(connector, repositoryNode));
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

  public RepositoryNodeCollection getChildren(String parentUrl) {
    // special handling for root
    if ("/".equals(parentUrl)) {
      return getRepoRootFolders();
    } 
    
    // First identify correct repo and truncate path to local part of
    // connector
    RepositoryConnector connector = getConnectorFromUrl(parentUrl);
    parentUrl = getRepositoryPartOfUrl(parentUrl);

      // now make the query
    RepositoryNodeCollection children = connector.getChildren(parentUrl);
   
    // and adjust the result to include repo name
    for (RepositoryNode repositoryNode : children.asList()) {
       adjust(connector, repositoryNode);
    }
    return children;
  }

  public RepositoryNodeCollection getRepoRootFolders() {
    ArrayList<RepositoryNode> nodes = new ArrayList<RepositoryNode>();
    for (RepositoryConnector connector : getRepositoryConnectors()) {
      String repoName = connector.getConfiguration().getName();
      RepositoryFolderImpl folder = new RepositoryFolderImpl(repoName);
      folder.getMetadata().setName(repoName);
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
      // TODO: Think about that a bit more, a bit hacky to depend on naming
      // conventions, or?
      // The GUI currently has types, maybe we should transport them to here?
      if (parameter.getKey().equals("targetFolder")) {
        if (parameter.getValue() instanceof String) {
          // folder id, I think the best for the moment.
          String targetFolderId = (String) parameter.getValue();
          parameters.put("targetFolderConnector", getConnectorFromUrl(targetFolderId));
          parameters.put("targetFolder", getRepositoryPartOfUrl(targetFolderId));
          
        } else if (parameter.getValue() instanceof RepositoryFolder) {
          throw new IllegalStateException("TArget Folder shouldn't be resolved by GUI any more");
        }
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
