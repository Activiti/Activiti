package org.activiti.cycle.impl.connector.demo;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.ContentRepresentationDefinition;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.connector.demo.action.CopyArtifactAction;
import org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration;
import org.activiti.cycle.impl.connector.view.CustomizedViewConfiguration;
import org.activiti.cycle.impl.util.RepositoryLogHelper;
import org.junit.Test;

public class DemoConnectorTest {

  @Test
  public void testFirstPlay() throws Exception {    
    // create demo connector but accessed via the customized view connector
    ConfigurationContainer userConfiguration = new ConfigurationContainer("bernd");
    userConfiguration.addRepositoryConnectorConfiguration(new DemoConnectorConfiguration("demo"));
    RepositoryConnector conn = new CustomizedViewConfiguration("http://localhost:8080/activiti-cycle/", userConfiguration).createConnector();
    
    List<RepositoryNode> childNodes = conn.getChildNodes("/");
    assertEquals(1, childNodes.size());
    assertEquals("demo", childNodes.get(0).getId());

    
    childNodes = conn.getChildNodes("demo");
    assertEquals(2, childNodes.size());

    assertEquals(RepositoryFolder.class, childNodes.get(0).getClass());
    RepositoryFolder folder1 = (RepositoryFolder) childNodes.get(0);
    assertEquals("/demo/minutes", folder1.getId());
    assertEquals("http://localhost:8080/activiti-cycle/demo/minutes", folder1.getClientUrl());
    
    assertEquals(RepositoryFolder.class, childNodes.get(1).getClass());
    RepositoryFolder folder2 = (RepositoryFolder) childNodes.get(1);
    assertEquals("/demo/BPMN", folder2.getId());
    
    // check sub elements of folder 1
    childNodes = conn.getChildNodes(folder1.getId());
    assertEquals(2, childNodes.size());

    RepositoryArtifact file1 = (RepositoryArtifact) childNodes.get(0);
    assertEquals("/demo/minutes/20100701-KickOffMeeting.txt", file1.getId());

    RepositoryArtifact file2 = (RepositoryArtifact) childNodes.get(1);
    assertEquals("/demo/minutes/InitialMindmap.mm", file2.getId());
    

    // check sub elements of folder 2
    childNodes = conn.getChildNodes(folder2.getId());
    assertEquals(1, childNodes.size());

    RepositoryFolder folder3 = (RepositoryFolder) childNodes.get(0);
    assertEquals("/demo/BPMN/Level3", folder3.getId());

    childNodes = conn.getChildNodes(folder3.getId());
    assertEquals(1, childNodes.size());

    RepositoryArtifact file3 = (RepositoryArtifact) childNodes.get(0);
    assertEquals("/demo/BPMN/Level3/789237892374239", file3.getId());
    assertEquals("InitialBpmnModel", file3.getMetadata().getName());
    assertEquals("/BPMN/Level3", file3.getMetadata().getPath());
    //
    // System.out.println(folder2.getId() + " -> " + folder2.getClientUrl());
    // System.out.println(folder3.getId() + " -> " + folder3.getClientUrl());
    // System.out.println(file3.getId() + " -> " + file3.getClientUrl());
    //    
    Collection<ContentRepresentationDefinition> contentRepresentations = file3.getContentRepresentationDefinitions();
    for (ContentRepresentationDefinition contentRepresentation : contentRepresentations) {
      System.out.println(contentRepresentation.getName() + " -> " + contentRepresentation.getClientUrl());
      System.out.println("  # FETCHED CONTENT VIA API: # " + conn.getContent(file3.getId(), contentRepresentation.getName()).asString());
    }

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("targetName", "xxx.txt");
    parameters.put("copyCount", 2);
    file1.executeAction(CopyArtifactAction.class.getName(), parameters);
    
    List<RepositoryNode> nodes = DemoConnector.nodes;
    childNodes = conn.getChildNodes(folder1.getId());
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
    
    RepositoryConnector conn = new CustomizedViewConfiguration("http://localhost:8080/activiti-cycle/", configuration).createConnector();
    
    RepositoryLogHelper.printNodes(conn.getChildNodes("/"));

  }
}
