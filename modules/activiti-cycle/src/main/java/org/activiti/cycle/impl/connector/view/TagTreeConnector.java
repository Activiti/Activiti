package org.activiti.cycle.impl.connector.view;

import java.util.List;
import java.util.Map;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;

/**
 * Connector to represent customized view for a user of cycle to hide all the
 * internal configuration and {@link RepositoryConnector} stuff from the client
 * (e.g. the webapp)
 * 
 * @author bernd.ruecker@camunda.com
 */
public class TagTreeConnector implements RepositoryConnector {

  private TagTreeConnectorConfiguration configuration;

  public TagTreeConnector(TagTreeConnectorConfiguration customizedViewConfiguration) {
    configuration = customizedViewConfiguration;
  }

  public TagTreeConnectorConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * TODO: Implement :-) But that gets actually a bit tricky, since we have
   * completely different ID's in this connector. So maybe that's not that easy
   * doable transparently as planned. So maybe this connector could be just read
   * only? Or it internally caches the id with tag and the repository name and
   * artifact id? Or...?
   * 
   * Discuss...
   */
  
  public void commitPendingChanges(String comment) {
  }

  public RepositoryArtifact createArtifact(String containingFolderId, String artifactName, String artifactType, Content artifactContent)
          throws RepositoryNodeNotFoundException {
    return null;
  }

  public RepositoryArtifact createArtifactFromContentRepresentation(String containingFolderId, String artifactName, String artifactType,
          String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
    return null;
  }

  public RepositoryFolder createFolder(String parentFolderId, String name) throws RepositoryNodeNotFoundException {
    return null;
  }

  public void deleteArtifact(String artifactId) throws RepositoryNodeNotFoundException {
  }

  public void deleteFolder(String folderId) throws RepositoryNodeNotFoundException {
  }

  public void executeParameterizedAction(String artifactId, String actionId, Map<String, Object> parameters) throws Exception {
  }

  public RepositoryNodeCollection getChildren(String id) throws RepositoryNodeNotFoundException {
    return null;
  }

  public Content getContent(String artifactId, String representationName) throws RepositoryNodeNotFoundException {
    return null;
  }

  public RepositoryArtifact getRepositoryArtifact(String id) throws RepositoryNodeNotFoundException {
    return null;
  }

  public Content getRepositoryArtifactPreview(String artifactId) throws RepositoryNodeNotFoundException {
    return null;
  }

  public RepositoryFolder getRepositoryFolder(String id) throws RepositoryNodeNotFoundException {
    return null;
  }

  public List<ArtifactType> getSupportedArtifactTypes(String folderId) {
    return null;
  }

  public boolean login(String username, String password) {
    return false;
  }

  public void updateContent(String artifactId, Content content) throws RepositoryNodeNotFoundException {
  }

  public void updateContent(String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException {
  }
  
}
