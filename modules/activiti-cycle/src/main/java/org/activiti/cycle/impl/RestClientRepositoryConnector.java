package org.activiti.cycle.impl;

import java.util.List;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;

/**
 * Wrapper for {@link RepositoryConnector} to set client url in objects
 * correctly for REST API
 * 
 * @author bernd.ruecker@camunda.com
 */
public class RestClientRepositoryConnector implements RepositoryConnector {
  
  private String repositoryName;

  private String baseUrl;
  
  private RepositoryConnector connector;

  public RestClientRepositoryConnector(String repositoryName, String baseUrl, RepositoryConnector connector) {
    this.repositoryName = repositoryName;
    this.baseUrl = baseUrl;
    this.connector = connector;
  }
  
  private RepositoryNode adjustClientUrl(RepositoryNode repositoryNode) {
    repositoryNode.setClientUrl(baseUrl + repositoryName + "/" + repositoryNode.getId());
    
    if (repositoryNode instanceof RepositoryArtifact) {
      List<ContentRepresentation> contentRepresentations = ((RepositoryArtifact) repositoryNode).getContentRepresentations();
      for (ContentRepresentation contentRepresentation : contentRepresentations) {
        adjustClientUrl(contentRepresentation);
      }
    }
    
    return repositoryNode;
  }
  
  private ContentRepresentation adjustClientUrl(ContentRepresentation content) {
    content.setClientUrl(baseUrl + repositoryName + "/" + content.getArtifact().getId() + "/content/" + content.getName());
    return content;
  }

  public void createNewFile(String folderUrl, RepositoryArtifact file) {
    connector.createNewFile(folderUrl, file);
  }

  public void createNewSubFolder(String parentFolderUrl, RepositoryFolder subFolder) {
    connector.createNewSubFolder(parentFolderUrl, subFolder);
  }

  public void deleteArtifact(String artifactUrl) {
    connector.deleteArtifact(artifactUrl);
  }

  public void deleteSubFolder(String subFolderUrl) {
    connector.deleteSubFolder(subFolderUrl);    
  }

  public List<RepositoryNode> getChildNodes(String parentUrl) {
    List<RepositoryNode> childNodes = connector.getChildNodes(parentUrl);
    for (RepositoryNode repositoryNode : childNodes) {
      adjustClientUrl(repositoryNode);
    }
    return childNodes;
  }
  
  public RepositoryArtifact getArtifactDetails(String id) {
    return (RepositoryArtifact) adjustClientUrl(connector.getArtifactDetails(id));
  }

  public boolean login(String username, String password) {
    return connector.login(username, password);
  }

  public ContentRepresentation getContent(String nodeId, String representationName) {
    return adjustClientUrl(connector.getContent(nodeId, representationName));
  }

  public void commitPendingChanges(String comment) {
    connector.commitPendingChanges(comment);
  }
}
