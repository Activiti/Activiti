package org.activiti.impl.cycle.connect.mock;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.activiti.impl.cycle.connect.RestClientRepositoryConnector;
import org.activiti.impl.cycle.connect.api.ContentRepresentation;
import org.activiti.impl.cycle.connect.api.RepositoryArtifact;
import org.activiti.impl.cycle.connect.api.RepositoryConnector;
import org.activiti.impl.cycle.connect.api.RepositoryFolder;
import org.activiti.impl.cycle.connect.api.RepositoryNode;
import org.junit.Test;

public class DemoConnectorTest {

  @Test
  public void testFirstPlay() {
    
    RepositoryConnector conn = new RestClientRepositoryConnector("demo-repo", "http://localhost:8080/activiti-cycle/", new DemoConnector());
    
    List<RepositoryNode> childNodes = conn.getChildNodes("/");
    assertEquals(2, childNodes.size());

    assertEquals(RepositoryFolder.class, childNodes.get(0).getClass());
    RepositoryFolder folder1 = (RepositoryFolder) childNodes.get(0);
    assertEquals("/meeting-minutes", folder1.getId());
    // TODO: Think about //
    assertEquals("http://localhost:8080/activiti-cycle/demo-repo//meeting-minutes", folder1.getClientUrl());
    
    assertEquals(RepositoryFolder.class, childNodes.get(1).getClass());
    RepositoryFolder folder2 = (RepositoryFolder) childNodes.get(1);
    assertEquals("/BPMN", folder2.getId());
    
    // check sub elements of folder 1
    childNodes = conn.getChildNodes(folder1.getId());
    assertEquals(2, childNodes.size());

    RepositoryArtifact file1 = (RepositoryArtifact) childNodes.get(0);
    assertEquals("/meeting-minutes/20100701-KickOffMeeting.doc", file1.getId());

    RepositoryArtifact file2 = (RepositoryArtifact) childNodes.get(1);
    assertEquals("/meeting-minutes/InitialMindmap.mm", file2.getId());
    

    // check sub elements of folder 2
    childNodes = conn.getChildNodes(folder2.getId());
    assertEquals(1, childNodes.size());

    RepositoryFolder folder3 = (RepositoryFolder) childNodes.get(0);
    assertEquals("/BPMN/Level3", folder3.getId());

    childNodes = conn.getChildNodes(folder3.getId());
    assertEquals(1, childNodes.size());

    RepositoryArtifact file3 = (RepositoryArtifact) childNodes.get(0);
    assertEquals("/BPMN/Level3/789237892374239", file3.getId());
    assertEquals("InitialBpmnModel", file3.getMetadata().getName());
    assertEquals("/BPMN/Level3", file3.getMetadata().getPath());
    
    List<ContentRepresentation> contentRepresentations = file3.getContentRepresentations();
    for (ContentRepresentation contentRepresentation : contentRepresentations) {
      System.out.println(contentRepresentation.getName() + " -> " + contentRepresentation.getClientUrl());
    }
    

    
  }
}
