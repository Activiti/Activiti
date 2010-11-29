package org.activiti.cycle.impl.connector.signavio.provider;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.activiti.cycle.impl.connector.signavio.util.NewSignavioSvgApiBuilder;
import org.activiti.cycle.impl.connector.signavio.util.SignavioSvgApiBuilder;

public class SvgApiProvider extends SignavioContentRepresentationProvider {

  @Override
  public void addValueToContent(Content content, SignavioConnector connector, RepositoryArtifact artifact) {
      String text = new SignavioSvgApiBuilder(connector, artifact).buildHtml();
      content.setValue(text);
  }

}
