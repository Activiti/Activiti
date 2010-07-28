package org.activiti.cycle.impl.connector.signavio.provider;

import org.activiti.cycle.ContentRepresentationType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.restlet.data.Response;
import org.restlet.resource.Representation;


public class PngProvider extends SignavioContentRepresentationProvider {

  public PngProvider() {
    super("PNG", ContentRepresentationType.IMAGE);
  }

  @Override
  public byte[] getContent(RepositoryArtifact artifact) {
    try {
      Response pngResponse = getJsonResponse(artifact, "/png");
      Representation imageData = pngResponse.getEntity();

      byte[] image = toBytes(imageData.getText());

      // if (log.isLoggable(Level.FINEST)) {
      // log.finest("PNG - byte result: " + image);
      // }

      return image;
    } catch (Exception ex) {
      throw new RepositoryException("Exception while accessing Signavio repository", ex);
    }
  }
  
  /**
   * for documentation (even if do not use it at the moment)
   */
  public String getModelAsPngUrl(RepositoryArtifact fileInfo) {
    return getConnector(fileInfo).getSignavioConfiguration().getModelUrl() + fileInfo.getId() + "/png?token=" + getConnector(fileInfo).getSecurityToken();
  }

}
