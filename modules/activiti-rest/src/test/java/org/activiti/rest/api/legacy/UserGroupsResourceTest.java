package org.activiti.rest.api.legacy;

import org.activiti.rest.BaseRestTestCase;
import com.fasterxml.jackson.databind.JsonNode;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class UserGroupsResourceTest extends BaseRestTestCase {

  public void testGetAllUserGroups() throws Exception {
    ClientResource client = getAuthenticatedClient("user/kermit/groups");
    Representation response = client.get();
    JsonNode responseNode = objectMapper.readTree(response.getStream());
    assertNotNull(responseNode);
    assertEquals(1, responseNode.get("total").asInt());
    assertEquals("Administrators", responseNode.get("data").get(0).get("name").asText());
  }
}
