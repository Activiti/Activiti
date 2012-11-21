package org.activiti.rest.api.process;

import org.activiti.engine.test.Deployment;
import org.activiti.rest.BaseRestTestCase;
import org.codehaus.jackson.JsonNode;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class ProcessDefinitionsResourceTest extends BaseRestTestCase {

  @Deployment
  public void testGetDefinitions() throws Exception {
    ClientResource client = getAuthenticatedClient("process-definitions?startableByUser=kermit");
    Representation response = client.get();
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(1, responseNode.get("total").asInt());
    JsonNode procesNode = responseNode.get("data").get(0);
    assertEquals("simpleProcess", procesNode.get("key").asText());
  }
  
  @Deployment
  public void testGetDefinitionsWithInvalidUser() throws Exception {
    ClientResource client = getAuthenticatedClient("process-definitions?startableByUser=test");
    Representation response = client.get();
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(0, responseNode.get("total").asInt());
  }
}
