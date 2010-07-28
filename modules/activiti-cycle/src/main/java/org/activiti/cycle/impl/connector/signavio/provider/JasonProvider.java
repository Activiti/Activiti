package org.activiti.cycle.impl.connector.signavio.provider;

import org.activiti.cycle.ContentRepresentationType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.restlet.data.Response;

public class JasonProvider extends SignavioContentRepresentationProvider {

  public JasonProvider() {
    super("Jason", ContentRepresentationType.TEXT);
  }

  public byte[] getContent(RepositoryArtifact artifact) {
      try {
        Response jsonResponse = getJsonResponse(artifact, "/json");
        // return new JSONObject(jsonResponse.getEntity().getText());

        return toBytes(jsonResponse.getEntity().getText());
      } catch (Exception ex) {
        throw new RepositoryException("Error while accessing Signavio repository", ex);
      }    
  }

}
