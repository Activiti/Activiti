package org.activiti.rest.api.legacy;

import org.activiti.rest.BaseRestTestCase;
import com.fasterxml.jackson.databind.JsonNode;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class GroupUsersResourceTest extends BaseRestTestCase {

  public void testGetAllGroupUsers() throws Exception {
    ClientResource client = getAuthenticatedClient("group/admin/users");
    Representation response = client.get();
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(1, responseNode.get("total").asInt());
    assertEquals("kermit", responseNode.get("data").get(0).get("id").asText());
  }
}
