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
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.api.RestUrls;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * @author Frederik Heremans
 */
public class GroupResourceTest extends BaseRestTestCase {

  /**
   * Test getting a single group.
   */
  public void testGetGroup() throws Exception {
    try {
      Group testGroup = identityService.newGroup("testgroup");
      testGroup.setName("Test group");
      testGroup.setType("Test type");
      identityService.saveGroup(testGroup);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "testgroup"));
      
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
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
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "unexisting"));
    try {
      client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a group with id 'unexisting'.", expected.getStatus().getDescription());
    }
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
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "testgroup"));
      
      Representation response = client.delete();
      assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      assertEquals(0, response.getSize());
      
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
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "unexisting"));
    
    try {
      client.delete();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a group with id 'unexisting'.", expected.getStatus().getDescription());
    }
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
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "testgroup"));
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", "Updated group");
      requestNode.put("type", "Updated type");
      
      Representation response = client.put(requestNode);
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
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
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "testgroup"));
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      
      Representation response = client.put(requestNode);
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
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
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "testgroup"));
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("name", (JsonNode) null);
      requestNode.put("type",(JsonNode) null);
      
      Representation response = client.put(requestNode);
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
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
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP, "unexisting"));
    
    try {
      client.put(objectMapper.createObjectNode());
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a group with id 'unexisting'.", expected.getStatus().getDescription());
    }
  }
}
