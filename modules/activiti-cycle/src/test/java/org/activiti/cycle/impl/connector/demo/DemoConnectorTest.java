package org.activiti.cycle.impl.connector.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.connector.demo.action.CopyArtifactAction;
import org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration;
import org.activiti.cycle.impl.connector.view.GlobalTreeConnectorConfiguration;
import org.activiti.cycle.impl.plugin.PluginFinder;
import org.activiti.cycle.impl.util.RepositoryLogHelper;
import org.junit.Before;
import org.junit.Test;

public class DemoConnectorTest {
  
  @Before
  public void init() {
    // TODO: Should be done in Bootstrapping
    PluginFinder.checkPluginInitialization();
  }

  @Test
  public void testFirstPlay() throws Exception {    
    // create demo connector but accessed via the customized view connector
    ConfigurationContainer userConfiguration = new ConfigurationContainer("bernd");
    userConfiguration.addRepositoryConnectorConfiguration(new DemoConnectorConfiguration("demo"));
    RepositoryConnector conn = new GlobalTreeConnectorConfiguration(userConfiguration).createConnector();
    
    List<RepositoryNode> childNodes = conn.getChildren("/").asList();
    assertEquals(1, childNodes.size());
    assertEquals("demo", childNodes.get(0).getId());

    
    childNodes = conn.getChildren("demo").asList();
    assertEquals(2, childNodes.size());

    assertTrue(childNodes.get(0) instanceof RepositoryFolder);
    RepositoryFolder folder1 = (RepositoryFolder) childNodes.get(0);
    assertEquals("/demo/minutes", folder1.getId());
    // assertEquals("http://localhost:8080/activiti-cycle/demo/minutes",
    // folder1.getClientUrl());
    
    assertTrue(childNodes.get(1) instanceof RepositoryFolder);
    RepositoryFolder folder2 = (RepositoryFolder) childNodes.get(1);
    assertEquals("/demo/BPMN", folder2.getId());
    
    // check sub elements of folder 1
    childNodes = conn.getChildren(folder1.getId()).asList();
    assertEquals(2, childNodes.size());

    RepositoryArtifact file1 = (RepositoryArtifact) childNodes.get(0);
    assertEquals("/demo/minutes/20100701-KickOffMeeting.txt", file1.getId());

    RepositoryArtifact file2 = (RepositoryArtifact) childNodes.get(1);
    assertEquals("/demo/minutes/InitialMindmap.mm", file2.getId());
    

    // check sub elements of folder 2
    childNodes = conn.getChildren(folder2.getId()).asList();
    assertEquals(1, childNodes.size());

    RepositoryFolder folder3 = (RepositoryFolder) childNodes.get(0);
    assertEquals("/demo/BPMN/Level3", folder3.getId());

    childNodes = conn.getChildren(folder3.getId()).asList();
    assertEquals(1, childNodes.size());

    RepositoryArtifact file3 = (RepositoryArtifact) childNodes.get(0);
    assertEquals("/demo/BPMN/Level3/InitialBpmnModel", file3.getId());
    assertEquals("InitialBpmnModel", file3.getMetadata().getName());
    assertEquals("/BPMN/Level3", file3.getMetadata().setParentFolderId());
    //
    // System.out.println(folder2.getId() + " -> " + folder2.getClientUrl());
    // System.out.println(folder3.getId() + " -> " + folder3.getClientUrl());
    // System.out.println(file3.getId() + " -> " + file3.getClientUrl());
    //    
    Collection<ContentRepresentation> contentRepresentations = file3.getArtifactType().getContentRepresentations();
    for (ContentRepresentation contentRepresentation : contentRepresentations) {
      Content content = conn.getContent(file3.getId(), contentRepresentation.getId());
      assertNotNull(content);
      assertNotNull(content.asByteArray());
    }

    assertEquals(6, DemoConnector.nodes.size());

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("targetName", "xxx.txt");
    parameters.put("copyCount", 2);
    conn.executeParameterizedAction(file1.getId(), CopyArtifactAction.class.getName(), parameters);
    
    List<RepositoryNode> nodes = DemoConnector.nodes;
    assertEquals(8, DemoConnector.nodes.size());
    
    childNodes = conn.getChildren(folder1.getId()).asList();
    assertEquals(4, childNodes.size());

    assertEquals("/demo/minutes/20100701-KickOffMeeting.txt", childNodes.get(0).getId());
    assertEquals("/demo/minutes/InitialMindmap.mm", childNodes.get(1).getId());
    assertEquals("/demo/minutes/xxx.txt0", childNodes.get(2).getId());
    assertEquals("xxx.txt0", childNodes.get(2).getMetadata().getName());
    assertEquals("/demo/minutes/xxx.txt1", childNodes.get(3).getId());
    assertEquals("xxx.txt1", childNodes.get(3).getMetadata().getName());
  }

  // @Test
  public void testPlay() {

    ConfigurationContainer configuration = new ConfigurationContainer("bernd");
    configuration.addRepositoryConnectorConfiguration(new DemoConnectorConfiguration("demo"));
    configuration.addRepositoryConnectorConfiguration(new SignavioConnectorConfiguration("signavio", "http://localhost:8080/activiti-modeler/"));
    configuration.addRepositoryConnectorConfiguration(new FileSystemConnectorConfiguration("files", new File("C:/temp")));
    
    RepositoryConnector conn = new GlobalTreeConnectorConfiguration(configuration).createConnector();
    
    RepositoryLogHelper.printNodes(conn, conn.getChildren("/").asList());

  }
}
