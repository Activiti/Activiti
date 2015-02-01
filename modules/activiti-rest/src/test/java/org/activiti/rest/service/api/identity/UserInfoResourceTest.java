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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * @author Frederik Heremans
 */
public class UserInfoResourceTest extends BaseSpringRestTestCase {

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
      
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO_COLLECTION, newUser.getId())), HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertTrue(responseNode.isArray());
      assertEquals(2, responseNode.size());
      
      boolean foundFirst = false;
      boolean foundSecond = false;
      
      for (int i=0; i<responseNode.size(); i++) {
        ObjectNode info = (ObjectNode) responseNode.get(i);
        assertNotNull(info.get("key").textValue());
        assertNotNull(info.get("url").textValue());
        
        if (info.get("key").textValue().equals("key1")) {
          foundFirst = true;
          assertTrue(info.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(
                  RestUrls.URL_USER_INFO, newUser.getId(), "key1")));
        } else if (info.get("key").textValue().equals("key2")) {
          assertTrue(info.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(
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
      
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, newUser.getId(), "key1")), HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertEquals("key1", responseNode.get("key").textValue());
      assertEquals("Value 1", responseNode.get("value").textValue());
      
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(
                  RestUrls.URL_USER_INFO, newUser.getId(), "key1")));
      
    } finally {
      
      // Delete user after test passes or fails
      if (savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  /**
   * Test getting the info for an unexisting user.
   */
  public void testGetInfoForUnexistingUser() throws Exception {
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, "unexisting", "key1")), HttpStatus.SC_NOT_FOUND));
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
      
      closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, "testuser", "key1")), HttpStatus.SC_NOT_FOUND));
      
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
      
      closeResponse(executeRequest(new HttpDelete(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, newUser.getId(), "key1")), HttpStatus.SC_NO_CONTENT));
      
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
      
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, newUser.getId(), "key1"));
      httpPut.setEntity(new StringEntity(requestNode.toString()));
      CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertEquals("key1", responseNode.get("key").textValue());
      assertEquals("Updated value", responseNode.get("value").textValue());
      
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(
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
    
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, "unexisting", "key1"));
    httpPut.setEntity(new StringEntity(requestNode.toString()));
    closeResponse(executeRequest(httpPut, HttpStatus.SC_NOT_FOUND));
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
     
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, "testuser", "key1"));
      httpPut.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPut, HttpStatus.SC_NOT_FOUND));
      
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
    closeResponse(executeRequest(new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, "unexisting", "key1")), HttpStatus.SC_NOT_FOUND));
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
      
      closeResponse(executeRequest(new HttpDelete(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO, "testuser", "key1")), HttpStatus.SC_NOT_FOUND));
      
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
      
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO_COLLECTION, "testuser"));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      CloseableHttpResponse response = executeRequest(httpPost, HttpStatus.SC_CREATED);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertEquals("key1", responseNode.get("key").textValue());
      assertEquals("Value 1", responseNode.get("value").textValue());
      
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(
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
      
      HttpPost httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO_COLLECTION, "testuser"));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
      
      // Test creating without key
      requestNode = objectMapper.createObjectNode();
      requestNode.put("value", "The value");
      
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_BAD_REQUEST));
      
      // Test creating an already existing info 
      identityService.setUserInfo(newUser.getId(), "key1", "The value");
      requestNode = objectMapper.createObjectNode();
      requestNode.put("key", "key1");
      requestNode.put("value", "The value");
      
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_CONFLICT));
      
      // Test creating info for unexisting user
      httpPost = new HttpPost(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_INFO_COLLECTION, "unexistinguser"));
      httpPost.setEntity(new StringEntity(requestNode.toString()));
      closeResponse(executeRequest(httpPost, HttpStatus.SC_NOT_FOUND));
      
    } finally {
      
      // Delete user after test passes or fails
      if (savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
}
