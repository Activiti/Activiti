package org.activiti.cycle.impl.connector.fs.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.ContentProviderImpl;
import org.activiti.cycle.impl.connector.fs.FileSystemConnector;
import org.activiti.cycle.impl.util.IoUtils;

public class FileBinaryContentProvider extends ContentProviderImpl {
  @Override
  public void addValueToContent(Content content, RepositoryConnector connector, RepositoryArtifact artifact) {
    String fileName = ((FileSystemConnector) connector).getConfiguration().getBasePath() + artifact.getOriginalNodeId();
    File file = new File(fileName);
    FileInputStream fis = null;

    try {
      fis = new FileInputStream(file);
      content.setValue(IoUtils.readBytes(fis));

    } catch (FileNotFoundException fnfe) {
      throw new RepositoryException("Unable to find artifact " + artifact, fnfe);
    } catch (IOException ioe) {
      throw new RepositoryException("Error while accessing artifact " + artifact, ioe);
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException ex) {
          // log but ignore exception on closing
          log.log(Level.WARNING, "Couldn't close file '" + fileName + "'. Ignoring.", ex);
        }
      }
    }
  }
}
