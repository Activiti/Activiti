package org.activiti.cycle.impl.connector.demo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentationDefinition;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.UnsupportedRepositoryOpperation;
import org.activiti.cycle.impl.connector.AbstractRepositoryConnector;
import org.activiti.cycle.impl.plugin.ActivitiCyclePluginRegistry;

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

  public static final String ARTIFACT_TYPE_TEXT = "ARTIFACT_TYPE_TEXT";
  public static final String ARTIFACT_TYPE_MINDMAP = "ARTIFACT_TYPE_MINDMAP";
  public static final String ARTIFACT_TYPE_BPMN_20 = "ARTIFACT_TYPE_BPMN_20";

  public void createDemoData() {
    // Folder minutes
    RepositoryFolder folder1 = createFolder("/minutes", "Meeting Minutes", "/");

    RepositoryArtifact file1 = createArtifact("/minutes/20100701-KickOffMeeting.txt", ARTIFACT_TYPE_TEXT, "20100701-KickOffMeeting", "/minutes");
    addContentRepresentation(file1, "Text", "/org/activiti/cycle/impl/connector/demo/demo-minutes.txt"); // was
    // http://www.apache.org/foundation/records/minutes/2008/board_minutes_2008_10_15.txt

    RepositoryArtifact file2 = createArtifact("/minutes/InitialMindmap.mm", ARTIFACT_TYPE_MINDMAP, "InitialMindmap", "/minutes");
    addContentRepresentation(file2, "Image", "/org/activiti/cycle/impl/connector/demo/mindmap.jpg"); // http://www.buzan.com.au/images/EnergyMindMap_big.jpg
    addContentRepresentation(file2, "Text", "/org/activiti/cycle/impl/connector/demo/mindmap.html"); // http://en.wikipedia.org/wiki/Energy

    rootNodes.add(folder1);
    nodes.add(folder1);
    nodes.add(file1);
    nodes.add(file2);

    // Folder BPMN

    RepositoryFolder folder2 = createFolder("/BPMN", "BPMN", "/");
    RepositoryFolder folder3 = createFolder("/BPMN/Level3", "Level3", "/BPMN");

    RepositoryArtifact file3 = createArtifact("/BPMN/Level3/789237892374239", ARTIFACT_TYPE_BPMN_20, "InitialBpmnModel", "/BPMN/Level3");
    addContentRepresentation(file3, "Image", "/org/activiti/cycle/impl/connector/demo/bpmn.png");
    // "http://www.bpm-guide.de/wp-content/uploads/2010/07/Incident-Management-collab.png");
    addContentRepresentation(file3, "XML", "/org/activiti/cycle/impl/connector/demo/engine-pool.xml");
    // "http://www.bpm-guide.de/wp-content/uploads/2010/07/engine-pool.xml");

    rootNodes.add(folder2);
    nodes.add(folder2);
    nodes.add(folder3);
    nodes.add(file3);
  }

  private RepositoryNode clone(RepositoryNode node) {
    if (node instanceof RepositoryFolder) {
      return clone((RepositoryFolder) node);
    } else {
      return clone((RepositoryArtifact) node);
    }
  }

  private RepositoryFolder createFolder(String id, String name, String parentPath) {
    if (!id.startsWith("/")) {
      id = "/" + id;
    }
    RepositoryFolder newFolder = new RepositoryFolder(this);
    newFolder.setId(id);
    newFolder.getMetadata().setName(name);
    newFolder.getMetadata().setPath(parentPath);
    return newFolder;
  }

  private RepositoryArtifact createArtifact(String id, String artifactTypeIdentifier, String name, String parentPath) {
    if (!id.startsWith("/")) {
      id = "/" + id;
    }
    RepositoryArtifact newArtifact = new RepositoryArtifact(this);
    newArtifact.setArtifactType(ActivitiCyclePluginRegistry.getArtifactTypeByIdentifier(artifactTypeIdentifier));
    newArtifact.setId(id);
    newArtifact.getMetadata().setName(name);
    newArtifact.getMetadata().setPath(parentPath);
    return newArtifact;
  }

  public void copyArtifact(RepositoryArtifact artifact, String targetName) {
    RepositoryArtifact copy = clone(artifact);
    copy.setId(targetName);
    nodes.add(copy);
    
    Collection<ContentRepresentationDefinition> contentRepresentationDefinitions = artifact.getContentRepresentationDefinitions();
    for (ContentRepresentationDefinition def : contentRepresentationDefinitions) {
      def.getName();
      Content cont = artifact.loadContent(def.getName());
      addContentRepresentation(copy, def.getType(), cont.asByteArray());
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
  public static RepositoryFolder clone(RepositoryFolder folder) {
    RepositoryFolder newFolder = new RepositoryFolder(folder.getConnector());
    newFolder.setId(folder.getId());
    newFolder.getMetadata().setName(folder.getMetadata().getName());
    newFolder.getMetadata().setPath(folder.getMetadata().getPath());
    return newFolder;
  }

  /**
   * In the demo connector we need to clone the objects, because we change ids
   * later
   */
  public static RepositoryArtifact clone(RepositoryArtifact artifact) {
    RepositoryArtifact newArtifact = new RepositoryArtifact(artifact.getConnector());
    newArtifact.setArtifactType(artifact.getArtifactType());
    newArtifact.setId(artifact.getId());
    newArtifact.getMetadata().setName(artifact.getMetadata().getName());
    newArtifact.getMetadata().setPath(artifact.getMetadata().getPath());
    return newArtifact;
  }
  

  private void addContentRepresentation(RepositoryArtifact artifact, String name, byte[] byteArray) {
    Map<String, byte[]> map = content.get(artifact.getId());
    if (map == null) {
      map = new HashMap<String, byte[]>();
      content.put(artifact.getId(), map);
    }

    map.put(name, byteArray);
  }
  
  private void addContentRepresentation(RepositoryArtifact artifact, String name, String contentSourceUrl) {
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
      
      addContentRepresentation(artifact, name, byteStream.toByteArray());

    } catch (Exception ex) {
      log.log(Level.SEVERE, "couldn't load content for artifact " + artifact + " from URL " + contentSourceUrl, ex);
    }
  }  

  public void createNewSubFolder(String parentFolderUrl, RepositoryFolder subFolder) {
    throw new UnsupportedRepositoryOpperation("unsupported by demo connector");
  }

  public void deleteArtifact(String artifactUrl) {
    throw new UnsupportedRepositoryOpperation("unsupported by demo connector");
  }

  public void deleteSubFolder(String subFolderUrl) {
    throw new UnsupportedRepositoryOpperation("unsupported by demo connector");
  }

  public List<RepositoryNode> getChildNodes(String parentUrl) {
    ArrayList<RepositoryNode> list = new ArrayList<RepositoryNode>();
    if ("/".equals(parentUrl)) {
      for (RepositoryNode node : rootNodes) {
        list.add(clone(node));
      }
    } else {
      for (RepositoryNode node : nodes) {
        if (node.getId().startsWith(parentUrl) && !node.getId().equals(parentUrl)) {
          // remove / at the end
          String remainingUrl = node.getId().substring(parentUrl.length() + 1);
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
    return list;
  }

  public List<RepositoryNode> getChildNodes(String parentUrl, boolean fetchDetails) {
    return getChildNodes(parentUrl);
  }

  public RepositoryArtifact getRepositoryArtifact(String id) {
    for (RepositoryNode node : nodes) {
      if (node.getId().equals(id) && node instanceof RepositoryArtifact) {
        return clone((RepositoryArtifact) node);
      }
    }
    throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, id);
  }
  
  public RepositoryFolder getRepositoryFolder(String id) {
    for (RepositoryNode node : nodes) {
      if (node.getId().equals(id) && node instanceof RepositoryFolder) {
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

  public Content getContent(String nodeId, String representationName) {
    return getRepositoryArtifact(nodeId).loadContent(representationName);
  }

  public void commitPendingChanges(String comment) {
  }

  public void createNewArtifact(String containingFolderId, RepositoryArtifact artifact, Content artifactContent) {
    nodes.add(artifact);
    // TODO: How do we now what we get?
    addContentRepresentation(artifact, ARTIFACT_TYPE_TEXT, artifactContent.asByteArray());
  }

  public void modifyArtifact(RepositoryArtifact artifact, ContentRepresentationDefinition artifactContent) {
  }
}
