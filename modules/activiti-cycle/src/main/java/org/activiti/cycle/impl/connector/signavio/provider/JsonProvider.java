package org.activiti.cycle.impl.connector.signavio.provider;

import org.activiti.cycle.ContentRepresentationType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.restlet.data.Response;

public class JsonProvider extends SignavioContentRepresentationProvider {

  public JsonProvider() {
    super("json", ContentRepresentationType.TEXT);
  }

  public byte[] getContent(RepositoryArtifact artifact) {
    try {
      Response jsonResponse = getJsonResponse(artifact, "/json");
      String jsonString = jsonResponse.getEntity().getText();
      return toBytes(jsonString);
    } catch (Exception ex) {
      throw new RepositoryException("Error while accessing Signavio repository", ex);
    }
  }

}
