package org.activiti.cycle.impl.connector.fs;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.activiti.cycle.ContentRepresentationDefinition;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
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
    userConfiguration.addRepositoryConnectorConfiguration(new FileSystemConnectorConfiguration("filesystem", File.listRoots()[0]));
    conn = userConfiguration.getConnector("filesystem");
  }
  @After
  public void cleanUp() {
    userConfiguration = null;
    conn = null;
  }

  @Test
  public void testFileSystemConnector() {
    List<RepositoryNode> nodes = conn.getChildNodes("");

    for (RepositoryNode repositoryNode : nodes) {
      System.out.println(repositoryNode);

      if (repositoryNode instanceof RepositoryArtifact) {
        RepositoryArtifact artifact = (RepositoryArtifact) repositoryNode;

        Collection<ContentRepresentationDefinition> contentRepresentations = artifact.getContentRepresentationDefinitions();
        for (ContentRepresentationDefinition contentRepresentation : contentRepresentations) {
          System.out.println(contentRepresentation.getName() + " -> " + contentRepresentation.getClientUrl());
          System.out.println("  # FETCHED CONTENT VIA API: # " + conn.getContent(artifact.getId(), contentRepresentation.getName()).asString());
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
