package org.activiti.cycle.impl.connector.fs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.RepositoryArtifactImpl;
import org.activiti.cycle.impl.RepositoryFolderImpl;
import org.activiti.cycle.impl.RepositoryNodeCollectionImpl;
import org.activiti.cycle.impl.connector.AbstractRepositoryConnector;

import eu.medsea.mimeutil.MimeUtil;

/**
 * TODO: Use correct {@link RepositoryNodeNotFoundException}.
 * 
 * @author ruecker
 */
public class FileSystemConnector extends AbstractRepositoryConnector<FileSystemConnectorConfiguration> {
  
  public FileSystemConnector(FileSystemConnectorConfiguration conf) {
    super(conf);
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
      return getArtifactInfo(getFileFromId(id));
    } catch (IOException ex) {
      throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryArtifact.class, id, ex);
    }
  }

  public RepositoryFolder getRepositoryFolder(String id) {
    try {
      return getFolderInfo(getFileFromId(id));
    } catch (IOException ex) {
      throw new RepositoryNodeNotFoundException(getConfiguration().getName(), RepositoryFolder.class, id, ex);
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
    File newSubFolder = new File(getFileFromId(parentFolderId), parentFolderId);
    if (!newSubFolder.mkdir()) {
      throw new RepositoryException("Unable to create subfolder " + parentFolderId + " in parentfolder " + parentFolderId);
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

    // TODO: Better way to check for mimetypes or file extensions.
    // See http://www.rgagnon.com/javadetails/java-0487.html or Alfresco Remote
    // Api (org.alfresco.repo.content.MimetypeMap)
    String mimeType = getMimeType(file);
    
    // TODO: We should have an extension to ArtifactType mapping somewhere
    ArtifactType artifactType = getConfiguration().getArtifactType(mimeType);

    RepositoryArtifactImpl artifact = new RepositoryArtifactImpl(id, artifactType, this);
    artifact.getMetadata().setName(file.getName());
        
    // TODO: CHECK Implementation
    artifact.getMetadata().setParentFolderId(getLocalPath(file.getParent()));
    
    artifact.getMetadata().setLastChanged(new Date(file.lastModified()));
    return artifact;
  }

  /**
   * TODO: Find a better way for mimetype, related to issue above. Version below
   * return whole string after first dot
   */
  private String getMimeType(File file) {
    
    // TODO: This has problems with e.g. "*.bpmn.xml"
    String extension = MimeUtil.getExtension(file);
    
    // so we overwrite it with a temporary hack
    // But this cannot recognize *.bpmn20.xml :-/
    String name = file.getName();
    if (name.indexOf(".") > 0) {
      return name.substring(name.lastIndexOf(".") + 1);
    }

    return extension;

    // MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    // MimeType m =
    // MimeUtil.getMostSpecificMimeType(MimeUtil.getMimeTypes(file));
    // MimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");

  }

  private RepositoryFolder getFolderInfo(File file) throws IOException {

    String id = getLocalPath(file.getCanonicalPath());
    if ("".equals(id)) {
      // root folder is again a special case
      id = "/";
    }
    RepositoryFolderImpl folder = new RepositoryFolderImpl(id);
    folder.getMetadata().setName(file.getName());
    // TODO: Implement
    // folder.getMetadata().setParentFolderId();
    
    folder.getMetadata().setLastChanged(new Date(file.lastModified()));

    return folder;
  }

  public RepositoryArtifact createArtifact(String containingFolderId, String artifactName, String artifactType, Content artifactContent)
          throws RepositoryNodeNotFoundException {
    File newFile = new File(getFileFromId(containingFolderId), artifactName);
    BufferedOutputStream bos = null;

    try {
      if (newFile.createNewFile()) {
        bos = new BufferedOutputStream(new FileOutputStream(newFile));
        bos.write(artifactContent.asByteArray());
      }
    } catch (IOException ioe) {
      throw new RepositoryException("Unable to create file '" + artifactName + " in folder " + containingFolderId, ioe);
    } finally {
      if (bos != null) {
        try {
          bos.close();
        } catch (IOException e) {
          throw new RepositoryException("Unable to create file " + artifactName + " in folder " + containingFolderId, e);
        }
      }
    }
    
    return getRepositoryArtifact(getRepositoryNodeId(containingFolderId, artifactName));
  }

  public RepositoryArtifact createArtifactFromContentRepresentation(String containingFolderId, String artifactName, String artifactType,
          String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException {
    return createArtifact(containingFolderId, artifactName, artifactType, artifactContent);
  }

  public void updateContent(String artifactId, Content content) throws RepositoryNodeNotFoundException {
    throw new UnsupportedOperationException("FileSystemConnector does not support modifying files!");
  }

  public void updateContent(String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException {
    throw new UnsupportedOperationException("FileSystemConnector does not support modifying files!");   
  }

  private String getLocalPath(String path) {    
    if ("".equals(getConfiguration().getBasePath())) {
      // if root is configured in Unix ("/" without trailing slash = "")
      return path;
    }
    else if (path.startsWith(getConfiguration().getBasePath())) {
      path = path.replace(getConfiguration().getBasePath(), "");
      // replace windows style slashes
      path = path.replace("\\", "/");
      return path;
    }
    throw new RepositoryException("Unable to determine local path! ('" + path + "')");
  }
}
