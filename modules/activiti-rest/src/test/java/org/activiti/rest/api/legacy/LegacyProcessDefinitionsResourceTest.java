package org.activiti.rest.api.legacy;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;
import org.activiti.rest.BaseRestTestCase;
import org.codehaus.jackson.JsonNode;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Test for all <b>Legacy</b> REST-operations related to Process Definitions:
 *  
 * GET /process-definitions 
 * 
 * @author Frederik Heremans
 */
public class LegacyProcessDefinitionsResourceTest extends BaseRestTestCase {

  @Deployment(resources={"org/activiti/rest/api/repository/oneTaskProcess.bpmn20.xml"})
  public void testGetDefinitions() throws Exception {
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
            .singleResult();
    
    ClientResource client = getAuthenticatedClient("process-definitions");
    Representation response = client.get();
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(1, responseNode.get("total").asInt());
    
    // Check process properties
    JsonNode procesNode = responseNode.get("data").get(0);
    assertEquals(definition.getId(), procesNode.get("id").getTextValue());
    assertEquals(definition.getKey(), procesNode.get("key").getTextValue());
    assertEquals(definition.getCategory(), procesNode.get("category").getTextValue());
    assertEquals(definition.getVersion(), procesNode.get("version").getIntValue());
    assertEquals(definition.getResourceName(), procesNode.get("resourceName").getTextValue());
    assertEquals(definition.getDeploymentId(), procesNode.get("deploymentId").getTextValue());
    assertFalse(procesNode.get("graphicNotationDefined").getBooleanValue());
    assertTrue(procesNode.get("diagramResourceName").isNull());
    assertTrue(procesNode.get("startFormResourceKey").isNull());
  }
  
  
  @Deployment
  public void testGetDefinitionsStartableByUser() throws Exception {
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
