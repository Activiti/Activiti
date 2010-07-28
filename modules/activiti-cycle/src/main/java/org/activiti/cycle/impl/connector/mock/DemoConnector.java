package org.activiti.cycle.impl.connector.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.ContentRepresentationProvider;
import org.activiti.cycle.ContentRepresentationType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.UnsupportedRepositoryOpperation;
import org.activiti.cycle.impl.RepositoryRegistry;

public class DemoConnector implements RepositoryConnector {

  static {
    nodes = new ArrayList<RepositoryNode>();
    rootNodes = new ArrayList<RepositoryNode>();
    content = new HashMap<RepositoryNode, Map<String, ContentRepresentation>>();
    
    registerMetaddata();
    createDemoData();
  }

  private String loggedInUser;

  private static List<RepositoryNode> nodes;
  private static List<RepositoryNode> rootNodes;

  private static Map<RepositoryNode, Map<String, ContentRepresentation>> content;

  // private static Map<RepositoryNode, List<RepositoryNode>> childNodes = new
  // HashMap<RepositoryNode, List<RepositoryNode>>();

  private static Logger log = Logger.getLogger(DemoConnector.class.getName());

  public static final String ARTIFACT_TYPE_TEXT = "ARTIFACT_TYPE_TEXT";
  public static final String ARTIFACT_TYPE_MINDMAP = "ARTIFACT_TYPE_MINDMAP";
  public static final String ARTIFACT_TYPE_BPMN_20 = "ARTIFACT_TYPE_BPMN_20";
  
  public static class TestProvider extends ContentRepresentationProvider {
    public TestProvider(String name) {
      super(name);
    }
    public ContentRepresentation createContentRepresentation(RepositoryArtifact artifact, boolean includeBinaryContent) {
      Map<String, ContentRepresentation> map = content.get(artifact);
      if (map != null) {
        return map.get(getName());
      }
      throw new RepositoryException("Couldn't find content representation '" + getName() + "' for artifact " + artifact.getId());
    }
  }  

  public static void registerMetaddata() {
    RepositoryRegistry.registerContentLinkProvider(ARTIFACT_TYPE_TEXT, new TestProvider("Text"));
    RepositoryRegistry.registerContentLinkProvider(ARTIFACT_TYPE_MINDMAP, new TestProvider("Image"));
    RepositoryRegistry.registerContentLinkProvider(ARTIFACT_TYPE_BPMN_20, new TestProvider("Image"));
    RepositoryRegistry.registerContentLinkProvider(ARTIFACT_TYPE_BPMN_20, new TestProvider("Text"));
  }

  public static void createDemoData() {
    { // folder one
      RepositoryFolder folder1 = new RepositoryFolder();
      folder1.setId("/meeting-minutes");
      folder1.getMetadata().setName("Meeting Minutes");
      folder1.getMetadata().setPath("/");

      RepositoryArtifact file1 = new RepositoryArtifact();
      file1.setId("/meeting-minutes/20100701-KickOffMeeting.doc");
      file1.getMetadata().setName("20100701-KickOffMeeting");
      // file1.setFileType(RepositoryRegistry.getFileTypeByIdentifier(XXX));

      addContentRepresentation(file1, ContentRepresentationType.TEXT, "Text",
              "http://www.apache.org/foundation/records/minutes/2008/board_minutes_2008_10_15.txt");

      RepositoryArtifact file2 = new RepositoryArtifact();
      file2.setId("/meeting-minutes/InitialMindmap.mm");
      file2.getMetadata().setName("InitialMindmap");
      // file1.setFileType(RepositoryRegistry.getFileTypeByIdentifier(XXX));

      addContentRepresentation(file2, ContentRepresentationType.IMAGE, "Image", "http://www.buzan.com.au/images/EnergyMindMap_big.jpg");
      addContentRepresentation(file2, ContentRepresentationType.HTML, "Content", "http://en.wikipedia.org/wiki/Energy");

      // ArrayList<RepositoryNode> children = new ArrayList<RepositoryNode>();
      // children.add(file1);
      // children.add(file2);
      //      
      // childNodes.put(folder1, children);
      rootNodes.add(folder1);

      nodes.add(folder1);
      nodes.add(file1);
      nodes.add(file2);
    }
    { // folder one
      RepositoryFolder folder1 = new RepositoryFolder();
      folder1.setId("/BPMN");
      folder1.getMetadata().setName("BPMN");
      folder1.getMetadata().setPath("/");

      RepositoryFolder folder2 = new RepositoryFolder();
      folder2.setId("/BPMN/Level3");
      folder2.getMetadata().setName("Level3");
      folder2.getMetadata().setPath("/BPMN");

      RepositoryArtifact file1 = new RepositoryArtifact();
      file1.setId("/BPMN/Level3/789237892374239");
      file1.getMetadata().setName("InitialBpmnModel");
      file1.getMetadata().setPath("/BPMN/Level3");
      // file1.setFileType(RepositoryRegistry.getFileTypeByIdentifier(XXX));

      addContentRepresentation(file1, ContentRepresentationType.IMAGE, "Image",
              "http://www.bpm-guide.de/wp-content/uploads/2010/07/Incident-Management-collab.png");
      addContentRepresentation(file1, ContentRepresentationType.XML, "XML", "http://www.bpm-guide.de/wp-content/uploads/2010/07/engine-pool.xml");

      // ArrayList<RepositoryNode> children1 = new ArrayList<RepositoryNode>();
      // children1.add(folder2);
      //
      // ArrayList<RepositoryNode> children2 = new ArrayList<RepositoryNode>();
      // children2.add(file1);
      //
      // childNodes.put(folder1, children1);
      // childNodes.put(folder2, children2);
      //
      rootNodes.add(folder1);

      nodes.add(folder1);
      nodes.add(folder2);
      nodes.add(file1);
    }
  }

  private static void addContentRepresentation(RepositoryArtifact artifact, String type, String name, String contentSourceUrl) {
    Map<String, ContentRepresentation> map = content.get(artifact);
    if (map==null) {
      map = new HashMap<String, ContentRepresentation>();
      content.put(artifact, map);
    }
    ContentRepresentation cp = new ContentRepresentation();
    
    cp.setName(name);
    cp.setType(type);
    cp.setArtifact(artifact);
    
    // TODO: set content
    // cp.setContent(content)
    
    map.put(name, cp);
    
    artifact.getContentRepresentations().add(cp);
  }

  public void createNewFile(String folderUrl, RepositoryArtifact file) {
    throw new UnsupportedRepositoryOpperation("unsupported by demo connector");
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
    if ("/".equals(parentUrl)) {
      return rootNodes;
    } else {
      ArrayList<RepositoryNode> list = new ArrayList<RepositoryNode>();
      for (RepositoryNode node : nodes) {
        if (node.getId().startsWith(parentUrl) && !node.getId().equals(parentUrl)) {
          String remainingUrl = node.getId().substring(parentUrl.length() + 1); // remove
                                                                                // "/"
          remainingUrl = remainingUrl.substring(0, remainingUrl.length() - 1); // remove
                                                                               // /
                                                                               // at
                                                                               // the
          // end
          if (!remainingUrl.contains("/")) {
            list.add(node);
          }
        }
      }
      return list;
    }
  }

  public List<RepositoryNode> getChildNodes(String parentUrl, boolean fetchDetails) {
    return getChildNodes(parentUrl);
  }

  public RepositoryNode getNodeDetails(String url) {
    for (RepositoryNode node : nodes) {
      if (node.getId().equals(url)) {
        return node;
      }
    }
    throw new RepositoryException("Couldn't find node with url '" + url + "'");
  }

  public boolean login(String username, String password) {
    log.fine("login called with user " + username + " and password " + password);
    loggedInUser = username;
    return true;
  }

  public ContentRepresentation getContent(String nodeId, String representationName) {
    return content.get(getNodeDetails(nodeId)).get(representationName);
  }
}
