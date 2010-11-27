package org.activiti.cycle.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;


public class RepositoryNodeCollectionImpl implements RepositoryNodeCollection {
  
  private List<RepositoryNode> children = null;

  public RepositoryNodeCollectionImpl(List<RepositoryNode> children) {
    this.children = children;
    // TODO: Sort automatically?
  }

  /**
   * returns all sub folders, which are resolved by a call to the
   * {@link RepositoryConnector} this {@link RepositoryNode} is connected to!
   * Please note, that this may result in an exception if the connector is not
   * linked or the repository not reachable
   */
  public List<RepositoryFolder> getFolderList() {
    ArrayList<RepositoryFolder> list = new ArrayList<RepositoryFolder>();
    for (RepositoryNode node : asList()) {
      if (node instanceof RepositoryFolder) {
        list.add((RepositoryFolder) node);
      }
    }
    return list;
  }

  /**
   * returns all artifacts, which are resolved by a call to the
   * {@link RepositoryConnector} this {@link RepositoryNode} is connected to!
   * Please note, that this may result in an exception if the connector is not
   * linked or the repository not reachable
   */
  public List<RepositoryArtifact> getArtifactList() {
    ArrayList<RepositoryArtifact> list = new ArrayList<RepositoryArtifact>();
    for (RepositoryNode node : asList()) {
      if (node instanceof RepositoryArtifact) {
        list.add((RepositoryArtifact) node);
      }
    }
    return list;
  }

  /**
   * returns all children, which are resolved by a call to the
   * {@link RepositoryConnector} this {@link RepositoryNode} is connected to!
   * Please note, that this may result in an exception if the connector is not
   * linked or the repository not reachable
   */
  public List<RepositoryNode> asList() {
    return children;
  }

  public boolean containsArtifact(String id) {
    return (getArtifact(id) != null);
  }

  /**
   * TODO: Duoble check that current path is the best option here!
   */
  public RepositoryArtifact getArtifact(String currentPath) {
    for (RepositoryArtifact file : getArtifactList()) {
      if (currentPath.equals(file.getGlobalUniqueId())) {
        return file;
      }
    }
    return null;
  }

  public RepositoryArtifact getArtifactByName(String name) {
    for (RepositoryArtifact file : getArtifactList()) {
      if (name.equals(file.getMetadata().getName())) {
        return file;
      }
    }
    return null;
  }
  
  public boolean containsFolder(String id) {
    return (getFolder(id) != null);
  }

  /**
   * TODO: Duoble check that current path is the best option here!
   */
  public RepositoryFolder getFolder(String currentPath) {
    for (RepositoryFolder folder : getFolderList()) {
      if (currentPath.equals(folder.getGlobalUniqueId())) {
        return folder;
      }
    }
    return null;
  }

  public RepositoryFolder getFolderByName(String name) {
    for (RepositoryFolder folder : getFolderList()) {
      if (name.equals(folder.getMetadata().getName())) {
        return folder;
      }
    }
    return null;
  }

  public void sort() {
    // TODO: Implement sorting
  }

}
