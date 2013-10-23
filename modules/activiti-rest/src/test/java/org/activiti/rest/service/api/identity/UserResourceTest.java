/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.rest.service.api.identity;

import org.activiti.engine.identity.User;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;


/**
 * @author Frederik Heremans
 */
public class UserResourceTest extends BaseRestTestCase {

  /**
   * Test getting a single user.
   */
  public void testGetUser() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER,
              newUser.getId()));
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("testuser", responseNode.get("id").getTextValue());
      assertEquals("Fred", responseNode.get("firstName").getTextValue());
      assertEquals("McDonald", responseNode.get("lastName").getTextValue());
      assertEquals("no-reply@activiti.org", responseNode.get("email").getTextValue());
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, newUser.getId())));
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  /**
   * Test getting an unexisting user.
   */
  public void testGetUnexistingUser() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, "unexisting"));
    
    try {
      client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a user with id 'unexisting'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test deleting a single user.
   */
  public void testDeleteUser() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER,
              newUser.getId()));
      Representation response = client.delete();
      assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      assertEquals(0L, response.getSize());
      
      
      // Check if user is deleted
      assertEquals(0, identityService.createUserQuery().userId(newUser.getId()).count());
      savedUser = null;
      
    } finally {
      
      // Delete user after test fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  /**
   * Test deleting an unexisting user.
   */
  public void testDeleteUnexistingUser() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, "unexisting"));
    
    try {
      client.delete();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a user with id 'unexisting'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test updating a single user.
   */
  public void testUpdateUser() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      ObjectNode taskUpdateRequest = objectMapper.createObjectNode();
      taskUpdateRequest.put("firstName", "Tijs");
      taskUpdateRequest.put("lastName", "Barrez");
      taskUpdateRequest.put("email", "no-reply@alfresco.org");
      taskUpdateRequest.put("password", "updatedpassword");
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER,
              newUser.getId()));
      Representation response = client.put(taskUpdateRequest);
      
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("testuser", responseNode.get("id").getTextValue());
      assertEquals("Tijs", responseNode.get("firstName").getTextValue());
      assertEquals("Barrez", responseNode.get("lastName").getTextValue());
      assertEquals("no-reply@alfresco.org", responseNode.get("email").getTextValue());
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, newUser.getId())));
      
      // Check user is updated in activiti
      newUser = identityService.createUserQuery().userId(newUser.getId()).singleResult();
      assertEquals("Barrez", newUser.getLastName());
      assertEquals("Tijs", newUser.getFirstName());
      assertEquals("no-reply@alfresco.org", newUser.getEmail());
      assertEquals("updatedpassword", newUser.getPassword());
      
    } finally {
      
      // Delete user after test fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  /**
   * Test updating a single user passing in no fields in the json, user should remain unchanged.
   */
  public void testUpdateUserNoFields() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      ObjectNode taskUpdateRequest = objectMapper.createObjectNode();
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER,
              newUser.getId()));
      Representation response = client.put(taskUpdateRequest);
      
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("testuser", responseNode.get("id").getTextValue());
      assertEquals("Fred", responseNode.get("firstName").getTextValue());
      assertEquals("McDonald", responseNode.get("lastName").getTextValue());
      assertEquals("no-reply@activiti.org", responseNode.get("email").getTextValue());
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, newUser.getId())));
      
      // Check user is updated in activiti
      newUser = identityService.createUserQuery().userId(newUser.getId()).singleResult();
      assertEquals("McDonald", newUser.getLastName());
      assertEquals("Fred", newUser.getFirstName());
      assertEquals("no-reply@activiti.org", newUser.getEmail());
      assertNull(newUser.getPassword());
      
    } finally {
      
      // Delete user after test fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  /**
   * Test updating a single user passing in no fields in the json, user should remain unchanged.
   */
  public void testUpdateUserNullFields() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      ObjectNode taskUpdateRequest = objectMapper.createObjectNode();
      taskUpdateRequest.putNull("firstName");
      taskUpdateRequest.putNull("lastName");
      taskUpdateRequest.putNull("email");
      taskUpdateRequest.putNull("password");
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER,
              newUser.getId()));
      Representation response = client.put(taskUpdateRequest);
      
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("testuser", responseNode.get("id").getTextValue());
      assertTrue(responseNode.get("firstName").isNull());
      assertTrue(responseNode.get("lastName").isNull());
      assertTrue(responseNode.get("email").isNull());
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, newUser.getId())));
      
      // Check user is updated in activiti
      newUser = identityService.createUserQuery().userId(newUser.getId()).singleResult();
      assertNull(newUser.getLastName());
      assertNull(newUser.getFirstName());
      assertNull(newUser.getEmail());
      
    } finally {
      
      // Delete user after test fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  /**
   * Test updating an unexisting user.
   */
  public void testUpdateUnexistingUser() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, "unexisting"));
    
    try {
      client.put(objectMapper.createObjectNode());
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a user with id 'unexisting'.", expected.getStatus().getDescription());
    }
  }
}
