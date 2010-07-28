package org.activiti.cycle.impl.connector.signavio.provider;

import org.activiti.cycle.ContentRepresentationType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;

public class Bpmn20Provider extends SignavioContentRepresentationProvider {

  public Bpmn20Provider() {
    super("BPMN 2.0", ContentRepresentationType.XML);
  }

  public byte[] getContent(RepositoryArtifact artifact) {
    try {
      Response jpdlResponse = getJsonResponse(artifact, "/bpmn2_0_xml");
      DomRepresentation xmlData = jpdlResponse.getEntityAsDom();
      String result = getXmlAsString(xmlData);
      
      log.finest("BPMN 2.0 String: " + result);
      
      return toBytes(result);
    } catch (Exception ex) {
      throw new RepositoryException("Exception while accessing Signavio repository", ex);
    }
  }

}
