package org.activiti.cycle.impl.connector.fs.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.util.IoUtils;

/**
 * TODO: This is a bit unhandy, think about refactoring that stuff to not need
 * different classes for it
 * 
 * @author ruecker
 */
public class FileSystemBinaryProvider extends FileSystemContentRepresentationProvider {

  public FileSystemBinaryProvider(String name, String mimeType) {
    super(name, mimeType, true);
  }

  @Override
  public void addValueToContent(Content content, RepositoryArtifact artifact) {

    File file = new File(getConnector(artifact).getConfiguration().getBasePath() + artifact.getId());
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
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

}
