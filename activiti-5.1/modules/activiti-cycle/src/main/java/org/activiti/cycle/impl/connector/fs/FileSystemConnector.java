package org.activiti.cycle.impl.connector.fs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.impl.RepositoryFolderImpl;
import org.activiti.cycle.impl.RepositoryNodeCollectionImpl;
import org.activiti.cycle.impl.connector.AbstractFileSystemBasedRepositoryConnector;

/**
 * TODO: Use correct {@link RepositoryNodeNotFoundException}.
 * 
 * @author ruecker
 */
@CycleComponent
public class FileSystemConnector extends AbstractFileSystemBasedRepositoryConnector<FileSystemConnectorConfiguration> {

  public FileSystemConnector() {
  }

  public boolean login(String username, String password) {
    // login is not need to access local file system, so everything we get is
    // OK!
    return true;
  }

  public RepositoryNodeCollection getChildren(String parentId) throws RepositoryNodeNotFoundException {
    File[] children = null;
    String path = "";

    List<RepositoryNode> childNodes = new ArrayList<RepositoryNode>();

    try {
      if (parentId == null || parentId.length() == 0 || "/".equals(parentId)) {
        // Go to root!
        // we need a trailing slash because otherwise it is considered to be a
        // relative path if you just provider "" on unix or "c:" on windows
        path = getConfiguration().getBasePath() + "/";
        children = new File(path).listFiles();
      } else {
        // Use base path!
        children = getFileFromId(parentId).listFiles();
      }

      for (File file : children) {
        if (file.isDirectory()) {
          childNodes.add(getFolderInfo(file));
        } else if (file.isFile()) {
          childNodes.add(getArtifactInfo(file));
        } else {
          throw new IllegalStateException("File '" + file + "' is neither a directory nor a file.");
        }
      }
    } catch (Exception ex) {
      throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, parentId, ex);
    }

    return new RepositoryNodeCollectionImpl(childNodes);
  }

  public RepositoryArtifact getRepositoryArtifact(String id) {
    try {
      File file = getFileFromId(id);
      if (!file.isFile()) {
        throw new RepositoryNodeNotFoundException(id + " is not a file");
      }
      return getArtifactInfo(file);
    } catch (IOException ex) {
      throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, id, ex);
    }
  }

  public RepositoryFolder getRepositoryFolder(String id) {
    try {
      File file = getFileFromId(id);
      if (!file.isDirectory()) {
        throw new RepositoryNodeNotFoundException(id + " is not a directory");
      }
      return getFolderInfo(file);
    } catch (IOException ex) {
      throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id, ex);
    }
  }

  public RepositoryNode getRepositoryNode(String id) throws RepositoryNodeNotFoundException {
    try {
      File file = getFileFromId(id);
      if (file.isDirectory()) {
        return getFolderInfo(file);
      }
      return getArtifactInfo(file);
    } catch (IOException ex) {
      throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryNode.class, id, ex);
    }
  }

  private File getFileFromId(String id) {
    return new File(getConfiguration().getBasePath() + id);
  }

  public void deleteArtifact(String artifactId) throws RepositoryNodeNotFoundException {
    File fileToDelete = getFileFromId(artifactId);
    if (deleteFile(fileToDelete)) {
      return;
    }

    throw new RepositoryException("Unable to delete file " + fileToDelete);
  }

  public RepositoryFolder createFolder(String parentFolderId, String name) throws RepositoryNodeNotFoundException {
    File newSubFolder = new File(getFileFromId(parentFolderId), name);
    if (!newSubFolder.mkdir()) {
      throw new RepositoryException("Unable to create subfolder '" + name + "' in parentfolder '" + parentFolderId + "'");
    }

    return getRepositoryFolder(getRepositoryNodeId(parentFolderId, name));
  }

  private String getRepositoryNodeId(String parentFolderId, String name) {
    if (parentFolderId.endsWith("/")) {
      // required for root folder
      return parentFolderId + name;
    } else {
      return parentFolderId + "/" + name;
    }
  }

  public void deleteFolder(String folderId) throws RepositoryNodeNotFoundException {
    File subFolderToDelete = getFileFromId(folderId);
    if (deleteFile(subFolderToDelete)) {
      return;
    }

    throw new RepositoryException("Unable to delete folder " + subFolderToDelete);
  }

  public void commitPendingChanges(String comment) {
    // do nothing
  }

  // delete file or directory, even non-empty ones.
  private boolean deleteFile(File path) {
    if (path.exists() && path.isAbsolute()) {
      File[] files = path.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          deleteFile(files[i]);
        } else {
          files[i].delete();
        }
      }
    }
    return path.delete();
  }

  private RepositoryArtifact getArtifactInfo(File file) throws IOException {
    String id = getLocalPath(file.getCanonicalPath());
    RepositoryArtifact artifact = getRepositoryArtifactForFileName(file.getName(), id);
    // TODO: CHECK Implementation
    artifact.getMetadata().setParentFolderId(getLocalPath(file.getParent()));
    artifact.getMetadata().setLastChanged(new Date(file.lastModified()));
    return artifact;
  }

  private RepositoryFolder getFolderInfo(File file) throws IOException {

    String id = getLocalPath(file.getCanonicalPath());
    if ("".equals(id)) {
      // root folder is again a special case
      id = "/";
    }
    RepositoryFolderImpl folder = new RepositoryFolderImpl(getConfiguration().getId(), id);
    folder.getMetadata().setName(file.getName());
    folder.getMetadata().setLastChanged(new Date(file.lastModified()));
    if (!"/".equals(id)) {
      folder.getMetadata().setParentFolderId(getLocalPath(file.getParent()));
    }
    return folder;
  }

  public RepositoryArtifact createArtifact(String parentFolderId, String artifactName, String artifactType, Content artifactContent)
          throws RepositoryNodeNotFoundException {
    File newFile = new File(getFileFromId(parentFolderId), artifactName);
    BufferedOutputStream bos = null;

    try {
      if (newFile.createNewFile()) {
        bos = new BufferedOutputStream(new FileOutputStream(newFile));
        bos.write(artifactContent.asByteArray());
      }
    } catch (IOException ioe) {
      throw new RepositoryException("Unable to create file '" + artifactName + " in folder " + parentFolderId, ioe);
    } finally {
      if (bos != null) {
        try {
          bos.close();
        } catch (IOException e) {
          throw new RepositoryException("Unable to create file " + artifactName + " in folder " + parentFolderId, e);
        }
      }
    }

    return getRepositoryArtifact(getRepositoryNodeId(parentFolderId, artifactName));
  }

  public RepositoryArtifact createArtifactFromContentRepresentation(String parentFolderId, String artifactName, String artifactType,
          String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
    return createArtifact(parentFolderId, artifactName, artifactType, artifactContent);
  }

  public void updateContent(String artifactId, Content content) throws RepositoryNodeNotFoundException {
    RepositoryArtifact artifact = getRepositoryArtifact(artifactId);
    String fileName = getConfiguration().getBasePath() + artifact.getNodeId();
    File newFile = new File(fileName);
    BufferedOutputStream bos = null;

    try {
      if (newFile.exists()) {
        bos = new BufferedOutputStream(new FileOutputStream(newFile));
        bos.write(content.asByteArray());
      } else {
        throw new RepositoryException("File '" + fileName + "' does not exist.");
      }
    } catch (IOException ioe) {
      throw new RepositoryException("Unable to update content of file '" + artifactId + "'", ioe);
    } finally {
      if (bos != null) {
        try {
          bos.close();
        } catch (IOException e) {
          throw new RepositoryException("Unable to update content of file '" + artifactId + "'", e);
        }
      }
    }
  }

  public void updateContent(String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException {
    updateContent(artifactId, content);
  }

  public Content getContent(String artifactId) throws RepositoryNodeNotFoundException {
    RepositoryArtifact artifact = getRepositoryArtifact(artifactId);
    Content content = new Content();
    String fileName = getConfiguration().getBasePath() + artifact.getNodeId();
    File file = new File(fileName);
    try {
      content.setValue(new FileInputStream(file));
      return content;
    } catch (FileNotFoundException fnfe) {
      throw new RepositoryException("Unable to find artifact " + artifact, fnfe);
    }
  }

  private String getLocalPath(String path) {
    if ("".equals(getConfiguration().getBasePath())) {
      // if root is configured in Unix ("/" without trailing slash = "")
      return path;
    } else if (path.startsWith(getConfiguration().getBasePath())) {
      path = path.replace(getConfiguration().getBasePath(), "");
      // replace windows style slashes
      path = path.replace("\\", "/");
      return path;
    }
    throw new RepositoryException("Unable to determine local path! ('" + path + "')");
  }

  public boolean isLoggedIn() {
    return true;
  }

}
