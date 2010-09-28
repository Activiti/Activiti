package org.activiti.cycle.impl.connector.demo.provider;

import java.util.Map;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.ContentProviderImpl;
import org.activiti.cycle.impl.connector.demo.DemoConnector;

public class DemoProvider extends ContentProviderImpl {
  
  private final String contentRepresentationName;

  public DemoProvider(String contentRepresentationName) {
    this.contentRepresentationName = contentRepresentationName;

  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void addValueToContent(Content content, RepositoryConnector connector, RepositoryArtifact artifact) {
    Map<String, byte[]> map = DemoConnector.content.get(artifact.getId());
    if (map != null) {
      content.setValue(map.get(contentRepresentationName));
      return;
    }
    throw new RepositoryException("Couldn't find content representation '" + contentRepresentationName + "' for artifact " + artifact.getId());
  }

}
