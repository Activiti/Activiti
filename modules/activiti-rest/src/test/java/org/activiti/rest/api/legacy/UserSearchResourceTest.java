package org.activiti.rest.api.legacy;

import org.activiti.rest.BaseRestTestCase;
import com.fasterxml.jackson.databind.JsonNode;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class UserSearchResourceTest extends BaseRestTestCase {

  public void testGetAllUsers() throws Exception {
    ClientResource client = getAuthenticatedClient("users");
    try {
      client.get();
      fail();
    } catch(Exception e) {
      // not allowed, should provide search text
    }
  }
  
  public void testGetUsers() throws Exception {
    ClientResource client = getAuthenticatedClient("users?searchText=erm");
    Representation response = client.get();
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(1, responseNode.get("total").asInt());
    JsonNode userNode = responseNode.get("data").get(0);
    assertEquals("Kermit", userNode.get("firstName").asText());
  }
}
