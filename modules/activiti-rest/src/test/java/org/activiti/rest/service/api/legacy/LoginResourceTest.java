package org.activiti.rest.service.api.legacy;

import org.activiti.rest.service.BaseRestTestCase;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class LoginResourceTest extends BaseRestTestCase {

  public void testLoginInvalidArgument() throws Exception {
    ClientResource client = getAuthenticatedClient("login");
    try {
      client.post(null);
      fail("Exception expected");
    } catch(ResourceException re) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, re.getStatus());
    }
  }
}
