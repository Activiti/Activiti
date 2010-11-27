package org.activiti.cycle.impl.connector.demo.provider;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.ContentProviderImpl;

public class ExceptionProvider extends ContentProviderImpl {

  @Override
  public void addValueToContent(Content content, RepositoryConnector connector, RepositoryArtifact artifact) {
    throw new RuntimeException("You wanted an exception, you get an exception :-)");
  }

}
