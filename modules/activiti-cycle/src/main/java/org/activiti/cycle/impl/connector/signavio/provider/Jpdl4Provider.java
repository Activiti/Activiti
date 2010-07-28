package org.activiti.cycle.impl.connector.signavio.provider;

import org.activiti.cycle.ContentRepresentationType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;

public class Jpdl4Provider extends SignavioContentRepresentationProvider {

  public Jpdl4Provider() {
    super("jpdl4", ContentRepresentationType.XML);
  }

  @Override
  public byte[] getContent(RepositoryArtifact artifact) {
    try {
      Response jpdlResponse = getJsonResponse(artifact, "/jpdl4");
      DomRepresentation xmlData = jpdlResponse.getEntityAsDom();
      String jpdl4AsString = getXmlAsString(xmlData);
      log.finest("JPDL4 String: " + jpdl4AsString);
      return toBytes(jpdl4AsString);
    } catch (Exception ex) {
      throw new RepositoryException("Exception while accessing Signavio repository", ex);
    }
  }

}
