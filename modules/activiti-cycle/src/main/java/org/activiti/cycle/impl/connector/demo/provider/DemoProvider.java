package org.activiti.cycle.impl.connector.demo.provider;

import java.util.Map;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentationProvider;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.connector.demo.DemoConnector;

public class DemoProvider extends ContentRepresentationProvider {

  public DemoProvider(String name, String type, boolean downloadable) {
    super(name, type, downloadable);
  }
  
  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [" + getContentRepresentationName() + "]";
  }

  @Override
  public void addValueToContent(Content content, RepositoryArtifact artifact) {
    Map<String, byte[]> map = DemoConnector.content.get(artifact.getId());
    if (map != null) {
      content.setValue(map.get(getContentRepresentationName()));
      return;
    }
    throw new RepositoryException("Couldn't find content representation '" + getContentRepresentationName() + "' for artifact " + artifact.getId());
  }

}
