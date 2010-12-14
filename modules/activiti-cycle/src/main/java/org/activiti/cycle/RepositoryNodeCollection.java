package org.activiti.cycle;

import java.util.List;


public interface RepositoryNodeCollection {

  /**
   * returns all sub folders, which are resolved by a call to the
   * {@link RepositoryConnector} this {@link RepositoryNode} is connected to!
   * Please note, that this may result in an exception if the connector is not
   * linked or the repository not reachable
   */
  public List<RepositoryFolder> getFolderList();

  /**
   * returns all artifacts, which are resolved by a call to the
   * {@link RepositoryConnector} this {@link RepositoryNode} is connected to!
   * Please note, that this may result in an exception if the connector is not
   * linked or the repository not reachable
   */
  public List<RepositoryArtifact> getArtifactList();

  /**
   * returns all children, which are resolved by a call to the
   * {@link RepositoryConnector} this {@link RepositoryNode} is connected to!
   * Please note, that this may result in an exception if the connector is not
   * linked or the repository not reachable
   */
  public List<RepositoryNode> asList();
  
  /**
   * sort the list in the default sorting, which means: First Folders, than Artifacts. 
   * Folder and Artifacts are alphabetically sorted.
   */
  public void sort();

  public boolean containsArtifact(String artifactId);

  public boolean containsFolder(String folderId);

  public RepositoryArtifact getArtifact(String artifactId);

  public RepositoryArtifact getArtifactByName(String artifactName);

  public RepositoryFolder getFolder(String folderId);

  public RepositoryFolder getFolderByName(String folderName);

}
