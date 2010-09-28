package org.activiti.cycle.impl.connector.fs.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.ContentProviderImpl;
import org.activiti.cycle.impl.connector.fs.FileSystemConnector;

public class TextFileContentProvider extends ContentProviderImpl {

  @Override
  public void addValueToContent(Content content, RepositoryConnector connector, RepositoryArtifact artifact) {
    String fileName = ((FileSystemConnector) connector).getConfiguration().getBasePath() + artifact.getId();
    File file = new File(fileName);
    try{
    	content.setValue(new FileInputStream(file));
  	} catch (FileNotFoundException fnfe) {
      throw new RepositoryException("Unable to find artifact " + artifact, fnfe);
    }
  }

}
