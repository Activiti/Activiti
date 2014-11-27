package org.activiti.rest.service.api.management;

import java.util.Iterator;
import java.util.Map;

import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test for all REST-operations related to the Job collection and a single
 * job resource.
 * 
 * @author Frederik Heremans
 */
public class PropertiesCollectionResourceTest extends BaseSpringRestTestCase {

  
  /**
   * Test getting the engine properties.
   */
  public void testGetProperties() throws Exception {
  	CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_PROPERTIES_COLLECTION)), HttpStatus.SC_OK);
    
    Map<String, String> properties = managementService.getProperties();
    
    JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
    closeResponse(response);
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