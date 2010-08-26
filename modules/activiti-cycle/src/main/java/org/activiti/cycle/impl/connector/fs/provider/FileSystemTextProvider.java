package org.activiti.cycle.impl.connector.fs.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;

public class FileSystemTextProvider extends FileSystemContentRepresentationProvider {

  public static final String NAME = "Text";

  public FileSystemTextProvider() {
    super(NAME, ContentType.TEXT, true);
  }

  @Override
  public void addValueToContent(Content content, RepositoryArtifact artifact) {
    File file = new File(getConnector(artifact).getConfiguration().getBasePath() + artifact.getId());
    BufferedReader br = null;
    StringBuilder sb = null;

    try {
      br = new BufferedReader(new FileReader(file));
      sb = new StringBuilder();

      String s;
      while ((s = br.readLine()) != null) {
        sb.append(s);
      }
    } catch (FileNotFoundException fnfe) {
      throw new RepositoryException("Unable to find artifact " + artifact, fnfe);
    } catch (IOException ioe) {
      throw new RepositoryException("Error while accessing artifact " + artifact, ioe);
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }

    content.setValue(sb.toString());
  }

}
