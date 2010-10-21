package org.activiti.cycle.impl.connector.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CycleService;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.impl.CycleServiceImpl;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.cycle.impl.connector.demo.action.CopyArtifactAction;
import org.activiti.cycle.impl.plugin.PluginFinder;
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
    ConfigurationContainer configurationContainer = new ConfigurationContainer("bernd");
    RepositoryConnectorConfiguration configuration = new DemoConnectorConfiguration("demo");
    configurationContainer.addRepositoryConnectorConfiguration(configuration);

    CycleService cycleService = new CycleServiceImpl(configurationContainer.getConnectorList());

    // TODO: Correct user / password handling!!!!
    cycleService.login("bernd", "bernd");

    List<RepositoryNode> childNodes = cycleService.getChildren("demo", "/").asList();
//    assertEquals(1, childNodes.size());
//    assertEquals("demo", childNodes.get(0).getCurrentPath());

//    childNodes = cycleService.getChildren("demo", "/").asList();
    assertEquals(2, childNodes.size());

    assertTrue(childNodes.get(0) instanceof RepositoryFolder);
    RepositoryFolder folder1 = (RepositoryFolder) childNodes.get(0);
    assertEquals("/minutes", folder1.getCurrentPath());
    // assertEquals("http://localhost:8080/activiti-cycle/demo/minutes",
    // folder1.getClientUrl());

    assertTrue(childNodes.get(1) instanceof RepositoryFolder);
    RepositoryFolder folder2 = (RepositoryFolder) childNodes.get(1);
    assertEquals("/BPMN", folder2.getCurrentPath());

    // check sub elements of folder 1
    childNodes = cycleService.getChildren("demo", folder1.getCurrentPath()).asList();
    assertEquals(2, childNodes.size());

    RepositoryArtifact file1 = (RepositoryArtifact) childNodes.get(0);
    assertEquals("/minutes/20100701-KickOffMeeting.txt", file1.getCurrentPath());

    RepositoryArtifact file2 = (RepositoryArtifact) childNodes.get(1);
    assertEquals("/minutes/InitialMindmap.mm", file2.getCurrentPath());

    // check sub elements of folder 2
    childNodes = cycleService.getChildren("demo", folder2.getCurrentPath()).asList();
    assertEquals(1, childNodes.size());

    RepositoryFolder folder3 = (RepositoryFolder) childNodes.get(0);
    assertEquals("/BPMN/Level3", folder3.getCurrentPath());

    childNodes = cycleService.getChildren("demo", folder3.getCurrentPath()).asList();
    assertEquals(1, childNodes.size());

    RepositoryArtifact file3 = (RepositoryArtifact) childNodes.get(0);
    assertEquals("/BPMN/Level3/InitialBpmnModel", file3.getCurrentPath());
    assertEquals("InitialBpmnModel", file3.getMetadata().getName());
    assertEquals("/BPMN/Level3", file3.getMetadata().setParentFolderId());
    //
    // System.out.println(folder2.getId() + " -> " + folder2.getClientUrl());
    // System.out.println(folder3.getId() + " -> " + folder3.getClientUrl());
    // System.out.println(file3.getId() + " -> " + file3.getClientUrl());
    //    
    Collection<ContentRepresentation> contentRepresentations = file3.getArtifactType().getContentRepresentations();
    for (ContentRepresentation contentRepresentation : contentRepresentations) {
      Content content = cycleService.getContent("demo", file3.getCurrentPath(), contentRepresentation.getId());
      assertNotNull(content);
      assertNotNull(content.asByteArray());
    }

    assertEquals(6, DemoConnector.nodes.size());

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("targetName", "xxx.txt");
    parameters.put("copyCount", 2);
    parameters.put("targetConnectorId", "demo");
    parameters.put("targetFolderId", "/minutes");

    cycleService.executeParameterizedAction("demo", file1.getCurrentPath(), CopyArtifactAction.class.getName(), parameters);

    List<RepositoryNode> nodes = DemoConnector.nodes;
    assertEquals(8, DemoConnector.nodes.size());

    childNodes = cycleService.getChildren("demo", folder1.getCurrentPath()).asList();
    assertEquals(4, childNodes.size());

    assertEquals("/minutes/20100701-KickOffMeeting.txt", childNodes.get(0).getCurrentPath());
    assertEquals("/minutes/InitialMindmap.mm", childNodes.get(1).getCurrentPath());
    assertEquals("/minutes/xxx.txt0", childNodes.get(2).getCurrentPath());
    assertEquals("xxx.txt0", childNodes.get(2).getMetadata().getName());
    assertEquals("/minutes/xxx.txt1", childNodes.get(3).getCurrentPath());
    assertEquals("xxx.txt1", childNodes.get(3).getMetadata().getName());
  }

  // @Test
  public void testPlay() {

    // ConfigurationContainer configuration = new
    // ConfigurationContainer("bernd");
    // configuration.addRepositoryConnectorConfiguration(new
    // DemoConnectorConfiguration("demo"));
    // configuration.addRepositoryConnectorConfiguration(new
    // SignavioConnectorConfiguration("signavio",
    // "http://localhost:8080/activiti-modeler/"));
    // configuration.addRepositoryConnectorConfiguration(new
    // FileSystemConnectorConfiguration("files", new File("C:/temp")));
    //    
    // RepositoryConnector conn = new
    // RootConnectorConfiguration(configuration).createConnector();
    //    
    // RepositoryLogHelper.printNodes(conn, conn.getChildren("/").asList());

  }
}
