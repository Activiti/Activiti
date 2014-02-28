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
public class UserInfoResourceTest extends BaseRestTestCase {

  /**
   * Test getting the collection of info for a user.
   */
  public void testGetUserInfoCollection() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      identityService.setUserInfo(newUser.getId(), "key1", "Value 1");
      identityService.setUserInfo(newUser.getId(), "key2", "Value 2");
      
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO_COLLECTION,
              newUser.getId()));
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertTrue(responseNode.isArray());
      assertEquals(2, responseNode.size());
      
      boolean foundFirst = false;
      boolean foundSecond = false;
      
      for(int i=0; i<responseNode.size(); i++) {
        ObjectNode info = (ObjectNode) responseNode.get(i);
        assertNotNull(info.get("key").getTextValue());
        assertNotNull(info.get("url").getTextValue());
        
        if(info.get("key").getTextValue().equals("key1")) {
          foundFirst = true;
          assertTrue(info.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(
                  RestUrls.URL_USER_INFO, newUser.getId(), "key1")));
        } else if(info.get("key").getTextValue().equals("key2")) {
          assertTrue(info.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(
                  RestUrls.URL_USER_INFO, newUser.getId(), "key2")));
          foundSecond = true;
        }
      }
      assertTrue(foundFirst);
      assertTrue(foundSecond);
      
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  /**
   * Test getting info for a user.
   */
  public void testGetUserInfo() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      identityService.setUserInfo(newUser.getId(), "key1", "Value 1");
      
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO,
              newUser.getId(), "key1"));
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertEquals("key1", responseNode.get("key").getTextValue());
      assertEquals("Value 1", responseNode.get("value").getTextValue());
      
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(
                  RestUrls.URL_USER_INFO, newUser.getId(), "key1")));
      
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  /**
   * Test getting the info for an unexisting user.
   */
  public void testGetInfoForUnexistingUser() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, "unexisting", "key1"));
    
    try {
      client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a user with id 'unexisting'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test getting the info for a user who doesn't have that info set
   */
  public void testGetInfoForUserWithoutInfo() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, "testuser", "key1"));
      try {
        client.get();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("User info with key 'key1' does not exists for user 'testuser'.", expected.getStatus().getDescription());
      }
      
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  /**
   * Test deleting info for a user.
   */
  public void testDeleteUserInfo() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      identityService.setUserInfo(newUser.getId(), "key1", "Value 1");
      
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO,
              newUser.getId(), "key1"));
      Representation response = client.delete();
      assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      assertEquals(0, response.getSize());
      
      // Check if info is actually deleted
      assertNull(identityService.getUserInfo(newUser.getId(), "key1"));
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  /**
   * Test update info for a user.
   */
  public void testUpdateUserInfo() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      identityService.setUserInfo(newUser.getId(), "key1", "Value 1");
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("value", "Updated value");
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO,
              newUser.getId(), "key1"));
      Representation response = client.put(requestNode);
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertEquals("key1", responseNode.get("key").getTextValue());
      assertEquals("Updated value", responseNode.get("value").getTextValue());
      
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(
                  RestUrls.URL_USER_INFO, newUser.getId(), "key1")));
      
      // Check if info is actually updated
      assertEquals("Updated value", identityService.getUserInfo(newUser.getId(), "key1"));
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  /**
   * Test update the info for an unexisting user.
   */
  public void testUpdateInfoForUnexistingUser() throws Exception {
    
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("value", "Updated value");
    
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, "unexisting", "key1"));
    
    try {
      client.put(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a user with id 'unexisting'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test deleting the info for a user who doesn't have that info set
   */
  public void testUpdateUnexistingInfo() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("value", "Updated value");
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, "testuser", "key1"));
      try {
        client.put(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("User info with key 'key1' does not exists for user 'testuser'.", expected.getStatus().getDescription());
      }
      
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  /**
   * Test deleting the info for an unexisting user.
   */
  public void testDeleteInfoForUnexistingUser() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, "unexisting", "key1"));
    
    try {
      client.delete();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a user with id 'unexisting'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test deleting the info for a user who doesn't have that info set
   */
  public void testDeleteInfoForUserWithoutInfo() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, "testuser", "key1"));
      try {
        client.delete();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("User info with key 'key1' does not exists for user 'testuser'.", expected.getStatus().getDescription());
      }
      
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  public void testCreateUserInfo() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("value", "Value 1");
      requestNode.put("key", "key1");
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO_COLLECTION, "testuser"));
      Representation response = client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertEquals("key1", responseNode.get("key").getTextValue());
      assertEquals("Value 1", responseNode.get("value").getTextValue());
      
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(
                  RestUrls.URL_USER_INFO, newUser.getId(), "key1")));
      
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  public void testCreateUserInfoExceptions() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;

      // Test creating without value
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("key", "key1");
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO_COLLECTION, "testuser"));
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("The value cannot be null.", expected.getStatus().getDescription());
      }
      client.release();
      
      // Test creating without key
      requestNode = objectMapper.createObjectNode();
      requestNode.put("value", "The value");
      
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
        assertEquals("The key cannot be null.", expected.getStatus().getDescription());
      }
      client.release();
      
      // Test creating an already existing info 
      identityService.setUserInfo(newUser.getId(), "key1", "The value");
      requestNode = objectMapper.createObjectNode();
      requestNode.put("key", "key1");
      requestNode.put("value", "The value");
      
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_CONFLICT, expected.getStatus());
        assertEquals("User info with key 'key1' already exists for this user.", expected.getStatus().getDescription());
      }
      client.release();
            
      
      // Test creating info for unexisting user
      client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO_COLLECTION, "unexistinguser"));
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("Could not find a user with id 'unexistinguser'.", expected.getStatus().getDescription());
      }
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
}
