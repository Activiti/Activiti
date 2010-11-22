package org.activiti.cycle.incubator.connector.vfs.provider;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.ContentProviderImpl;
import org.activiti.cycle.incubator.connector.vfs.VfsConnector;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;

/**
 * 
 * @author daniel.meyer@camunda.com
 */
public class VfsBinaryContentProvider extends ContentProviderImpl {

  public void addValueToContent(Content content, RepositoryConnector connector, RepositoryArtifact artifact) {

    VfsConnector vfsConnector = (VfsConnector) connector;
    String id = artifact.getNodeId();
    FileObject fileObject = null;
    try {

      FileSystemManager manager = vfsConnector.getFileSystemManager();

      fileObject = manager.resolveFile(vfsConnector.buildFilename(id));

      FileContent fileContent = fileObject.getContent();

      content.setValue(fileContent.getInputStream());

      // TODO: when do we close the file?

    } catch (FileSystemException e) {
      throw new RepositoryException("Error while getting content of " + id, e);
    }

  }

}
