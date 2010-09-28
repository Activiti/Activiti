package org.activiti.cycle.impl.connector.fs;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.plugin.PluginFinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileSystemConnectorTest {

  private static Logger log = Logger.getLogger(FileSystemConnectorTest.class.getName());

  private ConfigurationContainer userConfiguration;
  private RepositoryConnector conn;

  @Before
  public void initialize() throws IOException {
    userConfiguration = new ConfigurationContainer("christian");
    userConfiguration.addRepositoryConnectorConfiguration(new FileSystemConnectorConfiguration("filesystem", new File(".")));
    conn = userConfiguration.getConnector("filesystem");
    
      // TODO: Should be done in Bootstrapping
    PluginFinder.checkPluginInitialization();
  }
  
  @After
  public void cleanUp() {
    userConfiguration = null;
    conn = null;
  }

  @Test
  public void testFileSystemConnector() {
    List<RepositoryNode> nodes = conn.getChildren("").asList();

    for (RepositoryNode repositoryNode : nodes) {
      System.out.println(repositoryNode);

      if (repositoryNode instanceof RepositoryArtifact) {
        RepositoryArtifact artifact = (RepositoryArtifact) repositoryNode;

        Collection<ContentRepresentation> contentRepresentations = artifact.getArtifactType().getContentRepresentations();
        for (ContentRepresentation contentRepresentation : contentRepresentations) {
          System.out.println(contentRepresentation.getId() + " -> " + conn.getContent(artifact.getId(), contentRepresentation.getId()).asString());
        }
      }
    }

  }

  public static void printFileProperties(File file, String indent) throws IOException {
    System.out.println(indent + "Name: " + file.getName());
    System.out.println(indent + "AbsolutePath: " + file.getAbsolutePath());
    System.out.println(indent + "AbsoluteFile: " + file.getAbsoluteFile());
    System.out.println(indent + "CanonicalPath: " + file.getCanonicalPath());
    System.out.println(indent + "CanonicalFile: " + file.getCanonicalFile());
    System.out.println(indent + "Parent: " + file.getParent());
    System.out.println(indent + "Path: " + file.getPath());
    System.out.println(indent + "ParentFile: " + file.getParentFile());
    System.out.println(indent + "ToString: " + file.toString());
  }
}
