package org.activiti.cycle.impl.connector.fs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentationDefinition;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.connector.AbstractRepositoryConnector;
import org.activiti.cycle.impl.plugin.ActivitiCyclePluginRegistry;

import eu.medsea.mimeutil.MimeUtil;

/**
 * TODO: Use correct {@link RepositoryNodeNotFoundException}.
 * 
 * @author ruecker
 */
public class FileSystemConnector extends AbstractRepositoryConnector<FileSystemConnectorConfiguration> {

  public static final String BPMN_20_XML = "bpmn20.xml";
  public static final String ORYX_XML = "oryx.xml";
  public static final String TEXT = "txt";
  public static final String XML = "xml";
  public static final String MS_WORD = "doc";
  public static final String MS_WORD_X = "docx";
  public static final String MS_PP = "ppt";
  public static final String MS_PP_X = "pptx";
  public static final String PDF = "pdf";
  
  public FileSystemConnector(FileSystemConnectorConfiguration conf) {
    super(conf);
  }

  public boolean login(String username, String password) {
    // login is not need to access local file system, so everything we get is
    // OK!
    return true;
  }

  public List<RepositoryNode> getChildNodes(String parentId) {
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

    return childNodes;
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

  public Content getContent(String nodeId, String representationName) {
    RepositoryArtifact artifact = getRepositoryArtifact(nodeId);
    return artifact.loadContent(representationName);
  }

  public void deleteArtifact(String artifactId) {
    File fileToDelete = getFileFromId(artifactId);
    if (deleteFile(fileToDelete)) {
      return;
    }

    throw new RepositoryException("Unable to delete file " + fileToDelete);
  }

  public void createNewSubFolder(String parentFolderId, RepositoryFolder subFolder) {
    File newSubFolder = new File(getFileFromId(parentFolderId), subFolder.getId());
    if (!newSubFolder.mkdir()) {
      throw new RepositoryException("Unable to create subfolder " + subFolder.getId() + " in parentfolder " + parentFolderId);
    }
  }

  public void deleteSubFolder(String subFolderId) {
    File subFolderToDelete = getFileFromId(subFolderId);
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
    RepositoryArtifact artifact = new RepositoryArtifact(this);

    artifact.setId(getLocalPath(file.getCanonicalPath()));
    artifact.getMetadata().setName(file.getName());
    artifact.getMetadata().setPath(getConfiguration().getBasePath() + artifact.getId());
    artifact.getMetadata().setLastChanged(new Date(file.lastModified()));

    // FIXME: Better way to check for mimetypes or file extensions.
    // See http://www.rgagnon.com/javadetails/java-0487.html or Alfresco Remote
    // Api (org.alfresco.repo.content.MimetypeMap)
    artifact.setArtifactType(ActivitiCyclePluginRegistry.getArtifactTypeByIdentifier(getMimeType(file)));

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
    RepositoryFolder folder = new RepositoryFolder(this);

    String id = getLocalPath(file.getCanonicalPath());
    if ("".equals(id)) {
      // root folder is again a special case
      id = "/";
    }
    folder.setId(id);
    folder.getMetadata().setName(file.getName());
    folder.getMetadata().setPath(getConfiguration().getBasePath() + folder.getId());
    folder.getMetadata().setLastChanged(new Date(file.lastModified()));

    return folder;
  }

  public void createNewArtifact(String containingFolderId, RepositoryArtifact artifact, Content artifactContent) {
    File newFile = new File(getFileFromId(containingFolderId), artifact.getId());
    BufferedOutputStream bos = null;

    try {
      if (newFile.createNewFile()) {
        bos = new BufferedOutputStream(new FileOutputStream(newFile));
        bos.write(artifactContent.asByteArray());
      }
    } catch (IOException ioe) {
      throw new RepositoryException("Unable to create file " + artifact + " in folder " + containingFolderId);
    } finally {
      if (bos != null) {
        try {
          bos.close();
        } catch (IOException e) {
          throw new RepositoryException("Unable to create file " + artifact + " in folder " + containingFolderId);
        }
      }
    }
  }

  public void modifyArtifact(RepositoryArtifact artifact, ContentRepresentationDefinition artifactContent) {
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
