package org.activiti.cycle.impl.connector.fs.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
    try{
    	content.setValue(new FileInputStream(file));
  	} catch (FileNotFoundException fnfe) {
      throw new RepositoryException("Unable to find artifact " + artifact, fnfe);
    }
  }

}
