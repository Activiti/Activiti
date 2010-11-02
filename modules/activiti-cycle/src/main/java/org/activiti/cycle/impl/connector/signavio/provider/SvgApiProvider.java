package org.activiti.cycle.impl.connector.signavio.provider;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.activiti.cycle.impl.connector.signavio.util.SignavioSvgApiBuilder;
import org.json.JSONException;

public class SvgApiProvider extends SignavioContentRepresentationProvider {

  @Override
  public void addValueToContent(Content content, SignavioConnector connector, RepositoryArtifact artifact) {
    try {
      String text = new SignavioSvgApiBuilder(connector, artifact).buildHtml();
      content.setValue(text);
    } catch (JSONException ex) {
      throw new RepositoryException("Error while building svg api construct", ex);
    }    
  }

}
