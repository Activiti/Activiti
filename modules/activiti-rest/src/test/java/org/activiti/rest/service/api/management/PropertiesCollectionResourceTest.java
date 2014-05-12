package org.activiti.rest.service.api.management;

import java.util.Iterator;
import java.util.Map;

import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test for all REST-operations related to the Job collection and a single
 * job resource.
 * 
 * @author Frederik Heremans
 */
public class PropertiesCollectionResourceTest extends BaseRestTestCase {

  
  /**
   * Test getting the engine properties.
   */
  public void testGetProperties() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_PROPERTIES_COLLECTION));
    Representation response = client.get();
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    
    Map<String, String> properties = managementService.getProperties();
    
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(properties.size(), responseNode.size());
    
    Iterator<Map.Entry<String, JsonNode>> nodes = responseNode.fields();
    Map.Entry<String, JsonNode> node = null;
    while(nodes.hasNext()) {
      node = nodes.next();
      String propValue = properties.get(node.getKey());
      assertNotNull(propValue);
      assertEquals(propValue, node.getValue().textValue());
    }
  }
}