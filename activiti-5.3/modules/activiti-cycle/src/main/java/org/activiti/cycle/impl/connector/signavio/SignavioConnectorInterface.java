package org.activiti.cycle.impl.connector.signavio;

import java.io.IOException;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.restlet.Client;

/**
 * Public interface for the Signavio Connector.
 * 
 * (We need this because the {@link SignavioConnector} is proxied and we need to
 * be able to cast it to an interface such that these methods are visible.
 * 
 * @author daniel.meyer@camunda.com
 */
public interface SignavioConnectorInterface extends RepositoryConnector {
  
  public Client initClient();

  public String transformBpmn20XmltoJson(String xmlData);

  public String transformJsonToBpmn20Xml(String jsonData);

  public String getModelUrl(RepositoryArtifact artifact);

  public String getSecurityToken();

  public String getJsonResponse(String url) throws IOException;

  public SignavioConnectorConfiguration getConfiguration();

}
