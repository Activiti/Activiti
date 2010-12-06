package org.activiti.cycle.incubator.connector.vfs;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryAuthenticationException;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.RepositoryArtifactImpl;
import org.activiti.cycle.impl.RepositoryFolderImpl;
import org.activiti.cycle.impl.RepositoryNodeCollectionImpl;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.cycle.impl.connector.AbstractRepositoryConnector;
import org.activiti.cycle.impl.connector.util.ConnectorPathUtils;
import org.activiti.cycle.impl.connector.util.ConnectorStreamUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;

/**
 * Abstract base class for vfs-based connectors. Extend in order to implement
 * concrete protocols like (S)FTP etc.
 * 
 * Implementation Note: protocols need to be registered in the
 * {@link VfsConnectorConfiguration#createConnector()}-method.
 * 
 * @author daniel.meyer@camunda.com
 */
public abstract class VfsConnector extends AbstractRepositoryConnector<VfsConnectorConfiguration> {

  private static Logger log = Logger.getLogger(VfsConnector.class.getName());

  protected FileSystemManager fileSystemManager;

  protected String connectionString;

  protected FileSystemOptions fileSystemOptions;

  protected boolean loggedIn;

  public VfsConnector(VfsConnectorConfiguration configuration) {
    super(configuration);
    validateConfiguration();
  }

  public VfsConnector() {

  }

  public RepositoryArtifact getRepositoryArtifact(String id) throws RepositoryNodeNotFoundException {
    checkRepository();

    String filename = buildFilename(id);
    FileObject fileObject = null;
    RepositoryArtifact artifact = null;

    try {
      fileObject = fileSystemManager.resolveFile(filename);
      artifact = initRepositoryArtifact(fileObject, id);
      if (artifact == null)
        throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, id);
    } catch (FileSystemException e) {
      log.log(Level.WARNING, "cannot get artifact with id  " + id, e);
      throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, id, e);
    } finally {
      close(fileObject);
    }

    return artifact;
  }

  public RepositoryFolder getRepositoryFolder(String id) throws RepositoryNodeNotFoundException {
    checkRepository();

    String filename = buildFilename(id);
    FileObject fileObject = null;
    RepositoryFolder artifact = null;

    try {
      fileObject = fileSystemManager.resolveFile(filename);
      artifact = initRepositoryFolder(fileObject, id);
      if (artifact == null)
        throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id);
    } catch (FileSystemException e) {
      log.log(Level.WARNING, "cannot get artifact with id  " + id, e);
      throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id, e);
    } finally {
      close(fileObject);
    }

    return artifact;
  }

  public RepositoryNodeCollection getChildren(String id) throws RepositoryNodeNotFoundException {
    checkRepository();

    String filename = buildFilename(id);
    FileObject fileObject = null;
    FileObject[] children;
    List<RepositoryNode> result = new ArrayList<RepositoryNode>();

    try {
      fileObject = fileSystemManager.resolveFile(filename);
      children = fileObject.getChildren();
    } catch (FileSystemException e) {
      log.log(Level.WARNING, "cannot get Children of  " + id, e);
      throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id, e);
    } finally {
      close(fileObject);
    }

    for (FileObject child : children) {
      String newId = ConnectorPathUtils.buildId(id, child.getName().getBaseName());
      RepositoryNode node = initRepositoryNode(child, newId);
      if (node == null)
        continue;
      result.add(node);
      close(child);
    }

    return new RepositoryNodeCollectionImpl(result);
  }

  public RepositoryArtifact createArtifact(String parentFolderId, String artifactName, String artifactType, Content artifactContent)
          throws RepositoryNodeNotFoundException {
    checkRepository();

    String parentFolderName = buildFilename(parentFolderId);
    String artifactFileName = ConnectorPathUtils.buildId(parentFolderName, artifactName);
    FileObject newFile = null;
    try {
      newFile = fileSystemManager.resolveFile(artifactFileName);
      newFile.createFile();
      OutputStream os = newFile.getContent().getOutputStream();
      InputStream is = artifactContent.asInputStream();
      // closes stream
      ConnectorStreamUtils.copyStreams(is, os, 2024);

      return getRepositoryArtifact(ConnectorPathUtils.buildId(parentFolderId, artifactName));

    } catch (Exception e) {
      throw new RepositoryException("Could not create artifact '" + artifactName + "' in folder '" + parentFolderId + "'. Reason: " + e.getMessage(), e);
    }

  }

  public RepositoryArtifact createArtifactFromContentRepresentation(String parentFolderId, String artifactName, String artifactType,
          String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
    return createArtifact(parentFolderId, artifactName, artifactType, artifactContent);
  }

  public RepositoryFolder createFolder(String parentFolderId, String name) throws RepositoryNodeNotFoundException {
    checkRepository();

    String parentFolderName = buildFilename(parentFolderId);
    String folderFileName = ConnectorPathUtils.buildId(parentFolderName, name);
    FileObject newFolder = null;
    try {
      newFolder = fileSystemManager.resolveFile(folderFileName);
      newFolder.createFolder();

      return getRepositoryFolder(ConnectorPathUtils.buildId(parentFolderId, name));

    } catch (Exception e) {
      throw new RepositoryException("Could not create folder '" + name + "' in folder '" + parentFolderId + "'. Reason: " + e.getMessage(), e);
    }
  }

  public void updateContent(String artifactId, Content content) throws RepositoryNodeNotFoundException {
    checkRepository();

    String artifactFileName = buildFilename(artifactId);
    FileObject file = null;
    try {
      file = fileSystemManager.resolveFile(artifactFileName);

      OutputStream os = file.getContent().getOutputStream();
      InputStream is = content.asInputStream();
      // closes stream
      ConnectorStreamUtils.copyStreams(is, os, 2024);

    } catch (Exception e) {
      throw new RepositoryException("Could not update content of  artifact '" + artifactId + "'. Reason: " + e.getMessage(), e);
    }
  }

  public void updateContent(String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException {
    updateContent(artifactId, content);
  }

  public void deleteArtifact(String artifactId) throws RepositoryNodeNotFoundException {
    checkRepository();

    String artifactFileName = buildFilename(artifactId);
    FileObject file = null;
    try {
      file = fileSystemManager.resolveFile(artifactFileName);

      file.delete();

    } catch (Exception e) {
      throw new RepositoryException("Could not delete artifact '" + artifactId + "'. Reason: " + e.getMessage(), e);
    }
  }

  public void deleteFolder(String folderId) throws RepositoryNodeNotFoundException {
    checkRepository();

    String folderFileName = buildFilename(folderId);
    FileObject file = null;
    try {
      file = fileSystemManager.resolveFile(folderFileName);

      file.delete(new FileSelector() {

        public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
          // TODO: good idea?
          return true;
        }

        public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
          // TODO: good idea?
          return true;
        }
      });

    } catch (Exception e) {
      throw new RepositoryException("Could not delete folder '" + folderId + "'. Reason: " + e.getMessage(), e);
    }
  }

  protected RepositoryNode initRepositoryNode(FileObject child, String id) {
    FileType type = null;
    try {
      type = child.getType();
    } catch (FileSystemException e) {
      log.log(Level.WARNING, "Error while determining type of " + child.getName(), e);
      return null;
    }

    if (FileType.FOLDER.equals(type)) {
      return initRepositoryFolder(child, id);
    } else if (FileType.FILE.equals(type)) {
      return initRepositoryArtifact(child, id);
    }

    // TODO what about other types?
    log.info("could not determine whether " + child.getName() + " is a File or a directory.");
    return null;

  }

  protected RepositoryArtifact initRepositoryArtifact(FileObject child, String id) {
    String name = child.getName().getBaseName();
    if (name.equals(".") || name.equals(".."))
      return null;

    ArtifactType type = ConnectorPathUtils.getMimeType(child.getName().getBaseName(), getConfiguration());
    if (type == null)
      return null;

    RepositoryArtifact newArtifact = new RepositoryArtifactImpl(getConfiguration().getId(), id, type, this);
    newArtifact.getMetadata().setName(name);
    return newArtifact;
  }

  protected RepositoryFolder initRepositoryFolder(FileObject child, String id) {
    String name = child.getName().getBaseName();
    if (name.equals(".") || name.equals(".."))
      return null;
    RepositoryFolderImpl newFolder = new RepositoryFolderImpl(getConfiguration().getId(), id);
    newFolder.getMetadata().setName(name);
    return newFolder;
  }

  public String buildFilename(String id) {
    return ConnectorPathUtils.buildId(connectionString, id);
  }

  private void checkRepository() {
    getFileSystemManager();
    if (connectionString == null)
      throw new RepositoryException("You need to login first.");

  }

  protected void close(FileObject fileObject) {
    try {
      if (fileObject != null)
        fileObject.close();
    } catch (FileSystemException e) {
      log.log(Level.WARNING, "cannot close  " + fileObject.getName(), e);
    }
  }

  public boolean isLoggedIn() {
    return loggedIn;
  }

  @Override
  public void setConfiguration(RepositoryConnectorConfiguration configuration) {
    super.setConfiguration(configuration);
    validateConfiguration();
  }

  protected abstract void validateConfiguration();

  public abstract FileSystemManager getFileSystemManager();
}
