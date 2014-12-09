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
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * @author Frederik Heremans
 */
public class UserResourceTest extends BaseSpringRestTestCase {

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
      
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, newUser.getId())), HttpStatus.SC_OK);
      
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("testuser", responseNode.get("id").textValue());
      assertEquals("Fred", responseNode.get("firstName").textValue());
      assertEquals("McDonald", responseNode.get("lastName").textValue());
      assertEquals("no-reply@activiti.org", responseNode.get("email").textValue());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, newUser.getId())));
      
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
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, "unexisting")), HttpStatus.SC_NOT_FOUND));
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
      
      closeResponse(executeRequest(new HttpDelete(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, newUser.getId())), HttpStatus.SC_NO_CONTENT));
      
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
    closeResponse(executeRequest(new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, "unexisting")), HttpStatus.SC_NOT_FOUND));
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
      
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, newUser.getId()));
      httpPut.setEntity(new StringEntity(taskUpdateRequest.toString()));
      CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("testuser", responseNode.get("id").textValue());
      assertEquals("Tijs", responseNode.get("firstName").textValue());
      assertEquals("Barrez", responseNode.get("lastName").textValue());
      assertEquals("no-reply@alfresco.org", responseNode.get("email").textValue());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, newUser.getId())));
      
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
      
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, newUser.getId()));
      httpPut.setEntity(new StringEntity(taskUpdateRequest.toString()));
      CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("testuser", responseNode.get("id").textValue());
      assertEquals("Fred", responseNode.get("firstName").textValue());
      assertEquals("McDonald", responseNode.get("lastName").textValue());
      assertEquals("no-reply@activiti.org", responseNode.get("email").textValue());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, newUser.getId())));
      
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
      
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, newUser.getId()));
      httpPut.setEntity(new StringEntity(taskUpdateRequest.toString()));
      CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("testuser", responseNode.get("id").textValue());
      assertTrue(responseNode.get("firstName").isNull());
      assertTrue(responseNode.get("lastName").isNull());
      assertTrue(responseNode.get("email").isNull());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, newUser.getId())));
      
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
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, "unexisting"));
    httpPut.setEntity(new StringEntity(objectMapper.createObjectNode().toString()));
    closeResponse(executeRequest(httpPut, HttpStatus.SC_NOT_FOUND));
  }
}
