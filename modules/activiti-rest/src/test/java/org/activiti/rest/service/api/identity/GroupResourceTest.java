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

import org.activiti.engine.identity.Group;
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
public class GroupResourceTest extends BaseSpringRestTestCase {

  /**
   * Test getting a single group.
   */
  public void testGetGroup() throws Exception {
    try {
      Group testGroup = identityService.newGroup("testgroup");
      testGroup.setName("Test group");
      testGroup.setType("Test type");
      identityService.saveGroup(testGroup);
      
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "testgroup")), HttpStatus.SC_OK);
      
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("testgroup", responseNode.get("id").textValue());
      assertEquals("Test group", responseNode.get("name").textValue());
      assertEquals("Test type", responseNode.get("type").textValue());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_GROUP, testGroup.getId())));      
      
      Group createdGroup  = identityService.createGroupQuery().groupId("testgroup").singleResult();
      assertNotNull(createdGroup);
      assertEquals("Test group", createdGroup.getName());
      assertEquals("Test type", createdGroup.getType());
      
    } finally {
      try {
        identityService.deleteGroup("testgroup");
      } catch(Throwable ignore) {
        // Ignore, since the group may not have been created in the test
        // or already deleted
      }
    }
  }
  
  /**
   * Test getting an unexisting group.
   */
  public void testGetUnexistingGroup() throws Exception {
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "unexisting")), HttpStatus.SC_NOT_FOUND));
  }
  
  /**
   * Test deleting a single group.
   */
  public void testDeleteGroup() throws Exception {
    try {
      Group testGroup = identityService.newGroup("testgroup");
      testGroup.setName("Test group");
      testGroup.setType("Test type");
      identityService.saveGroup(testGroup);
      
      closeResponse(executeRequest(new HttpDelete(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "testgroup")), HttpStatus.SC_NO_CONTENT));
      
      assertNull(identityService.createGroupQuery().groupId("testgroup").singleResult());
      
    } finally {
      try {
        identityService.deleteGroup("testgroup");
      } catch(Throwable ignore) {
        // Ignore, since the group may not have been created in the test
        // or already deleted
      }
    }
  }
  
  /**
   * Test deleting an unexisting group.
   */
  public void testDeleteUnexistingGroup() throws Exception {
    closeResponse(executeRequest(new HttpDelete(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "unexisting")), HttpStatus.SC_NOT_FOUND));
  }
  
  /**
   * Test updating a single group.
   */
  public void testUpdateGroup() throws Exception {
    try {
      Group testGroup = identityService.newGroup("testgroup");
      testGroup.setName("Test group");
      testGroup.setType("Test type");
      identityService.saveGroup(testGroup);
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", "Updated group");
      requestNode.put("type", "Updated type");
      
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "testgroup"));
      httpPut.setEntity(new StringEntity(requestNode.toString()));
      CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
      
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("testgroup", responseNode.get("id").textValue());
      assertEquals("Updated group", responseNode.get("name").textValue());
      assertEquals("Updated type", responseNode.get("type").textValue());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_GROUP, testGroup.getId())));      
      
      Group createdGroup  = identityService.createGroupQuery().groupId("testgroup").singleResult();
      assertNotNull(createdGroup);
      assertEquals("Updated group", createdGroup.getName());
      assertEquals("Updated type", createdGroup.getType());
      
    } finally {
      try {
        identityService.deleteGroup("testgroup");
      } catch(Throwable ignore) {
        // Ignore, since the group may not have been created in the test
        // or already deleted
      }
    }
  }
  
  /**
   * Test updating a single group passing in no fields in the json, user should remain unchanged.
   */
  public void testUpdateGroupNoFields() throws Exception {
    try {
      Group testGroup = identityService.newGroup("testgroup");
      testGroup.setName("Test group");
      testGroup.setType("Test type");
      identityService.saveGroup(testGroup);
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "testgroup"));
      httpPut.setEntity(new StringEntity(requestNode.toString()));
      CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
      
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("testgroup", responseNode.get("id").textValue());
      assertEquals("Test group", responseNode.get("name").textValue());
      assertEquals("Test type", responseNode.get("type").textValue());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_GROUP, testGroup.getId())));      
      
      Group createdGroup  = identityService.createGroupQuery().groupId("testgroup").singleResult();
      assertNotNull(createdGroup);
      assertEquals("Test group", createdGroup.getName());
      assertEquals("Test type", createdGroup.getType());
      
    } finally {
      try {
        identityService.deleteGroup("testgroup");
      } catch(Throwable ignore) {
        // Ignore, since the group may not have been created in the test
        // or already deleted
      }
    }
  }
  
  /**
   * Test updating a single user passing in null-values.
   */
  public void testUpdateGroupNullFields() throws Exception {
    try {
      Group testGroup = identityService.newGroup("testgroup");
      testGroup.setName("Test group");
      testGroup.setType("Test type");
      identityService.saveGroup(testGroup);
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", (JsonNode) null);
      requestNode.put("type",(JsonNode) null);
      
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "testgroup"));
      httpPut.setEntity(new StringEntity(requestNode.toString()));
      CloseableHttpResponse response = executeRequest(httpPut, HttpStatus.SC_OK);
      JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
      closeResponse(response);
      assertNotNull(responseNode);
      assertEquals("testgroup", responseNode.get("id").textValue());
      assertNull(responseNode.get("name").textValue());
      assertNull(responseNode.get("type").textValue());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_GROUP, testGroup.getId())));      
      
      Group createdGroup  = identityService.createGroupQuery().groupId("testgroup").singleResult();
      assertNotNull(createdGroup);
      assertNull(createdGroup.getName());
      assertNull(createdGroup.getType());
      
    } finally {
      try {
        identityService.deleteGroup("testgroup");
      } catch(Throwable ignore) {
        // Ignore, since the group may not have been created in the test
        // or already deleted
      }
    }
  }
  
  /**
   * Test updating an unexisting group.
   */
  public void testUpdateUnexistingGroup() throws Exception {
    HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "unexisting"));
    httpPut.setEntity(new StringEntity(objectMapper.createObjectNode().toString()));
    closeResponse(executeRequest(httpPut, HttpStatus.SC_NOT_FOUND));
  }
}
