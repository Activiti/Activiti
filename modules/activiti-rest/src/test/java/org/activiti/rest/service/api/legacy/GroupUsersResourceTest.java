package org.activiti.rest.service.api.legacy;

import org.activiti.rest.service.BaseRestTestCase;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.fasterxml.jackson.databind.JsonNode;

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
