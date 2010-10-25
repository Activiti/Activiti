package org.activiti.cycle.impl.connector.demo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.RepositoryArtifactImpl;
import org.activiti.cycle.impl.RepositoryFolderImpl;
import org.activiti.cycle.impl.RepositoryNodeCollectionImpl;
import org.activiti.cycle.impl.connector.AbstractRepositoryConnector;

public class DemoConnector extends AbstractRepositoryConnector<DemoConnectorConfiguration> {

  public DemoConnector(DemoConnectorConfiguration configuration) {
    super(configuration);

    nodes = new ArrayList<RepositoryNode>();
    rootNodes = new ArrayList<RepositoryNode>();
    content = new HashMap<String, Map<String, byte[]>>();

    createDemoData(); 
  }

  private String loggedInUser;

  public static List<RepositoryNode> nodes;
  public static List<RepositoryNode> rootNodes;

  public static Map<String, Map<String, byte[]>> content;

  private static Logger log = Logger.getLogger(DemoConnector.class.getName());

  public void createDemoData() {
    // Folder minutes
    RepositoryFolder folder1 = createFolder("/", "minutes");

    RepositoryArtifact file1 = createArtifact("/minutes", "20100701-KickOffMeeting.txt", DemoConnectorPluginDefinition.ARTIFACT_TYPE_TEXT,
            createContent("/org/activiti/cycle/impl/connector/demo/demo-minutes.txt"));

    RepositoryArtifact file2 = createArtifact("/minutes", "InitialMindmap.mm", DemoConnectorPluginDefinition.ARTIFACT_TYPE_MINDMAP,
            createContent("/org/activiti/cycle/impl/connector/demo/mindmap.html"));
    addContentToInternalMap(file2.getNodeId(), DemoConnectorPluginDefinition.CONTENT_REPRESENTATION_ID_PNG,
            createContent(
            "/org/activiti/cycle/impl/connector/demo/mindmap.jpg").asByteArray());

    rootNodes.add(folder1);

    // Folder BPMN

    RepositoryFolder folder2 = createFolder("/", "BPMN");
    RepositoryFolder folder3 = createFolder("/BPMN", "Level3");

    RepositoryArtifact file3 = createArtifactFromContentRepresentation("/BPMN/Level3", "InitialBpmnModel", DemoConnectorPluginDefinition.ARTIFACT_TYPE_BPMN_20,
            DemoConnectorPluginDefinition.CONTENT_REPRESENTATION_ID_XML,
            createContent("/org/activiti/cycle/impl/connector/demo/engine-pool.xml"));
    addContentToInternalMap(file3.getNodeId(), DemoConnectorPluginDefinition.CONTENT_REPRESENTATION_ID_PNG, createContent(
            "/org/activiti/cycle/impl/connector/demo/bpmn.png").asByteArray());

    rootNodes.add(folder2);
    // nodes.add(folder2);
    // nodes.add(folder3);
    // nodes.add(file3);
  }

  private RepositoryNode clone(RepositoryNode node) {
    if (node instanceof RepositoryFolder) {
      return clone((RepositoryFolder) node);
    } else {
      return clone((RepositoryArtifact) node);
    }
  }

  public void copyArtifact(RepositoryArtifact artifact, String targetName) {
    RepositoryArtifact copy = new RepositoryArtifactImpl(getConfiguration().getId(), targetName, artifact.getArtifactType(), this);
    
    nodes.add(copy);
    
    Collection<ContentRepresentation> contentRepresentationDefinitions = artifact.getArtifactType().getContentRepresentations();
    for (ContentRepresentation def : contentRepresentationDefinitions) {
      def.getId();
      Content cont = getContent(artifact.getNodeId(), def.getId());
      addContentToInternalMap(copy.getNodeId(), def.getId(), cont.asByteArray());
    }
  }

  /**
   * In the demo connector we need to clone the objects, because we change ids
   * later
   * 
   * TODO: Maybe the view connector should do the cloning? Because he causes the
   * trouble. Can we avoid cloning in the other connectors because
   * {@link RepositoryNode} objects are considered one time only data containes?
   */
  public static RepositoryFolderImpl clone(RepositoryFolder folder) {
    // TODO: Maybe make deep copy?
    RepositoryFolderImpl newFolder = new RepositoryFolderImpl(folder.getConnectorId(), folder.getNodeId());
    
    newFolder.getMetadata().setName(folder.getMetadata().getName());
    newFolder.getMetadata().setParentFolderId(folder.getMetadata().setParentFolderId());
    return newFolder;
  }

  /**
   * In the demo connector we need to clone the objects, because we change ids
   * later
   */
  public RepositoryArtifactImpl clone(RepositoryArtifact artifact) {
    RepositoryArtifactImpl newArtifact = new RepositoryArtifactImpl(artifact.getConnectorId(), artifact.getNodeId(), artifact.getArtifactType(), this);

    newArtifact.getMetadata().setName(artifact.getMetadata().getName());
    newArtifact.getMetadata().setParentFolderId(artifact.getMetadata().setParentFolderId());
    
    return newArtifact;
  }
  

  private void addContentToInternalMap(String artifactId, String name, byte[] byteArray) {
    Map<String, byte[]> map = content.get(artifactId);
    if (map == null) {
      map = new HashMap<String, byte[]>();
      content.put(artifactId, map);
    }

    map.put(name, byteArray);
  }
  
  public Content getContent(String artifactId, String representationName) throws RepositoryNodeNotFoundException {
    Map<String, byte[]> map = content.get(artifactId);
    byte[] bs = map.get(representationName);
    Content c = new Content();
    c.setValue(bs);
    return c;
  }  
  
  private Content createContent(String contentSourceUrl) {
    Content result = new Content();
    try {
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

      // read locally instead of internet
      // InputStream in = new URL(contentSourceUrl).openStream();
      InputStream in = DemoConnector.class.getResourceAsStream(contentSourceUrl);
      if (in == null) {
        throw new RuntimeException("resource '" + contentSourceUrl + "' not found in classpath");
      }
      byte[] buf = new byte[512];
      int len;
      while (true) {
        len = in.read(buf);
        if (len == -1) {
          break;
        }
        byteStream.write(buf, 0, len);
      }
      byteStream.close();
      result.setValue(byteStream.toByteArray());
    } catch (Exception ex) {
      throw new RepositoryException("couldn't create content from URL " + contentSourceUrl, ex);
    }
    return result;      
  }  
  
  public RepositoryNodeCollection getChildren(String parentUrl) throws RepositoryNodeNotFoundException {
    ArrayList<RepositoryNode> list = new ArrayList<RepositoryNode>();
    if ("/".equals(parentUrl)) {
      for (RepositoryNode node : rootNodes) {
        list.add(clone(node));
      }
    } else {
      for (RepositoryNode node : nodes) {
        if (node.getNodeId().startsWith(parentUrl) && !node.getNodeId().equals(parentUrl)) {
          // remove / at the end
          String remainingUrl = node.getNodeId().substring(parentUrl.length() + 1);
          remainingUrl = remainingUrl.substring(0, remainingUrl.length() - 1);
          
          if (!remainingUrl.contains("/")) {
            list.add(clone(node));
          }
        }
      }
      if (list.size() == 0) {
        throw new RepositoryNodeNotFoundException(RepositoryNodeNotFoundException.createChildrenNotFoundMessage(getConfiguration().getName(),
                RepositoryFolder.class, parentUrl));
      }
    }
    return new RepositoryNodeCollectionImpl(list);
  }

  public RepositoryArtifact getRepositoryArtifact(String id) {
    for (RepositoryNode node : nodes) {
      if (node.getNodeId().equals(id) && node instanceof RepositoryArtifact) {
        return clone((RepositoryArtifact) node);
      }
    }
    throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, id);
  }
  
  public RepositoryFolder getRepositoryFolder(String id) {
    for (RepositoryNode node : nodes) {
      if (node.getNodeId().equals(id) && node instanceof RepositoryFolder) {
        return clone((RepositoryFolder) node);
      }
    }
    throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id);
  }  

  public boolean login(String username, String password) {
    log.fine("login called with user " + username + " and password " + password);
    loggedInUser = username;
    return true;
  }
  
  public void commitPendingChanges(String comment) {
  }

  public RepositoryArtifact createArtifact(String containingFolderId, String artifactName, String artifactType, Content artifactContent)
          throws RepositoryNodeNotFoundException {
    return createArtifactFromContentRepresentation(containingFolderId, artifactName, artifactType, getArtifactType(artifactType)
            .getDefaultContentRepresentation().getId(),
            artifactContent);
  }

  public RepositoryArtifact createArtifactFromContentRepresentation(String containingFolderId, String artifactName, String artifactType,
          String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
    String id = containingFolderId;
    if (!id.startsWith("/")) {
      id = "/" + id;
    }
    if (id.length()>1) {
      // all but root folder
      id = id + "/";
    }
    id = id + artifactName;
    
    RepositoryArtifact newArtifact = new RepositoryArtifactImpl(getConfiguration().getId(), id, getArtifactType(artifactType), this);
    newArtifact.getMetadata().setName(artifactName);
    newArtifact.getMetadata().setParentFolderId(containingFolderId);
    nodes.add(newArtifact);

    addContentToInternalMap(id, contentRepresentationName, artifactContent.asByteArray());
    return newArtifact;
  }

  public void updateContent(String artifactId, Content artifactContent) {
    updateContent(artifactId, getRepositoryArtifact(artifactId).getArtifactType().getDefaultContentRepresentation().getId(), artifactContent);
  }

  public void updateContent(String artifactId, String contentRepresentationId, Content artifactContent) {
    addContentToInternalMap(artifactId, contentRepresentationId, artifactContent.asByteArray());
  }  
  
  public RepositoryFolder createFolder(String parentFolderId, String name) throws RepositoryNodeNotFoundException {
    String id = parentFolderId;
    if (!id.startsWith("/")) {
      id = "/" + id;
    }
    if (id.length() > 1) {
      // all but root folder
      id = id + "/";
    }
    id = id + name;
    RepositoryFolderImpl newFolder = new RepositoryFolderImpl(getConfiguration().getId(), id);

    newFolder.getMetadata().setName(name);
    newFolder.getMetadata().setParentFolderId(parentFolderId);
    nodes.add(newFolder);

    return newFolder;
  }

  public void deleteArtifact(String artifactId) throws RepositoryNodeNotFoundException {
    throw new RepositoryException("Not implemented in DemoConnector");
  }

  public void deleteFolder(String folderId) throws RepositoryNodeNotFoundException {
    throw new RepositoryException("Not implemented in DemoConnector");
  }

  public void exceuteAction(String artifactId, String actionId, Map<String, Object> parameters) {
  }
}
