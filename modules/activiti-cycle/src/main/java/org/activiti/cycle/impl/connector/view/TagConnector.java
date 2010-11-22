package org.activiti.cycle.impl.connector.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.Content;
import org.activiti.cycle.CycleTagContent;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.RepositoryFolderImpl;
import org.activiti.cycle.impl.RepositoryNodeCollectionImpl;

/**
 * Connector to represent customized view for a user of cycle to hide all the
 * internal configuration and {@link RepositoryConnector} stuff from the client
 * (e.g. the webapp)
 * 
 * @author bernd.ruecker@camunda.com
 */
public class TagConnector implements RepositoryConnector {

  private TagConnectorConfiguration configuration;

  public TagConnector(TagConnectorConfiguration customizedViewConfiguration) {
    configuration = customizedViewConfiguration;
  }

  public TagConnectorConfiguration getConfiguration() {
    return configuration;
  }

  public void commitPendingChanges(String comment) {
  }

  public List<ArtifactType> getSupportedArtifactTypes(String folderId) {
    return new ArrayList<ArtifactType>();
  }

  public boolean login(String username, String password) {
    return true;
  }
  
  /**
   * only operation making sense, since the tag connector "just" introduces tga
   * folders
   */
  public RepositoryNodeCollection getChildren(String id) throws RepositoryNodeNotFoundException {
    if ("/".equals(id)) {
      return new RepositoryNodeCollectionImpl(createRootFolders());
    } else {
      String name = id.substring(id.lastIndexOf("/") + 1);
      CycleTagContent tagContent = getConfiguration().getCycleService().getTagContent(name);
      return new RepositoryNodeCollectionImpl(tagContent.getTaggedRepositoryNodes());
    }
  }

  private List<RepositoryNode> createRootFolders() {
    List<RepositoryNode> tagFolderList = new ArrayList<RepositoryNode>();

    List<CycleTagContent> rootTags = getConfiguration().getCycleService().getRootTags();
    for (CycleTagContent tag : rootTags) {
      RepositoryFolderImpl folder = new RepositoryFolderImpl(getConfiguration().getId(), tag.getName());
      folder.getMetadata().setName(tag.getName() + " [" + tag.getUsageCount() + "]");
      tagFolderList.add(folder);
    }

    return tagFolderList;
  }
  
  public RepositoryArtifact createArtifact(String parentFolderId, String artifactName, String artifactType, Content artifactContent)
          throws RepositoryNodeNotFoundException {
    throw new UnsupportedOperationException("Cannot create artifact in TagConnector, use real RepositoryConnector istead.");
  }

  public RepositoryArtifact createArtifactFromContentRepresentation(String parentFolderId, String artifactName, String artifactType,
          String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
    throw new UnsupportedOperationException("Cannot create artifact in TagConnector, use real RepositoryConnector istead.");
  }

  public RepositoryFolder createFolder(String parentFolderId, String name) throws RepositoryNodeNotFoundException {
    throw new UnsupportedOperationException("Cannot create folder in TagConnector, use real RepositoryConnector istead.");
  }

  public void deleteArtifact(String artifactId) throws RepositoryNodeNotFoundException {
    throw new UnsupportedOperationException("Cannot delete artifact in TagConnector, use real RepositoryConnector istead.");
  }

  public void deleteFolder(String folderId) throws RepositoryNodeNotFoundException {
    throw new UnsupportedOperationException("Cannot delete folder in TagConnector, use real RepositoryConnector istead.");
  }

  public void executeParameterizedAction(String artifactId, String actionId, Map<String, Object> parameters) throws Exception {
    throw new UnsupportedOperationException("Cannot execute action in TagConnector, use real RepositoryConnector istead.");
  }

  public Content getContent(String artifactId, String representationName) throws RepositoryNodeNotFoundException {
    throw new UnsupportedOperationException("Cannot get content in TagConnector, use real RepositoryConnector istead.");
  }

  public RepositoryArtifact getRepositoryArtifact(String id) throws RepositoryNodeNotFoundException {
    throw new UnsupportedOperationException("Cannot get artifact in TagConnector, use real RepositoryConnector istead.");
  }

  public Content getRepositoryArtifactPreview(String artifactId) throws RepositoryNodeNotFoundException {
    throw new UnsupportedOperationException("Cannot get artifact in TagConnector, use real RepositoryConnector istead.");
  }

  public RepositoryFolder getRepositoryFolder(String id) throws RepositoryNodeNotFoundException {
    throw new UnsupportedOperationException("Cannot get folder in TagConnector, use real RepositoryConnector istead.");
  }

  public void updateContent(String artifactId, Content content) throws RepositoryNodeNotFoundException {
    throw new UnsupportedOperationException("Cannot update content in TagConnector, use real RepositoryConnector istead.");
  }

  public void updateContent(String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException {
    throw new UnsupportedOperationException("Cannot update content in TagConnector, use real RepositoryConnector istead.");    
  }

  public void beginTransaction() {
  } 
  
}
