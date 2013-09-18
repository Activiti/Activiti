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
public class GroupMembershipResourceTest extends BaseRestTestCase {

  public void testCreatemembership() throws Exception {
    try {
      Group testGroup = identityService.newGroup("testgroup");
      testGroup.setName("Test group");
      testGroup.setType("Test type");
      identityService.saveGroup(testGroup);
      
      User testUser = identityService.newUser("testuser");
      identityService.saveUser(testUser);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP_MEMBERSHIP_COLLECTION, "testgroup"));
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("userId", "testuser");
      
      Representation response = client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("testuser", responseNode.get("userId").getTextValue());
      assertEquals("testgroup", responseNode.get("groupId").getTextValue());
      assertTrue(responseNode.get("url").getTextValue().endsWith(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_GROUP_MEMBERSHIP, testGroup.getId(), testUser.getId())));      
      
      Group createdGroup  = identityService.createGroupQuery().groupId("testgroup").singleResult();
      assertNotNull(createdGroup);
      assertEquals("Test group", createdGroup.getName());
      assertEquals("Test type", createdGroup.getType());
      
      assertNotNull(identityService.createUserQuery().memberOfGroup("testgroup").singleResult());
      assertEquals("testuser", identityService.createUserQuery().memberOfGroup("testgroup").singleResult().getId());
    } finally {
      try {
        identityService.deleteGroup("testgroup");
      } catch(Throwable ignore) {
        // Ignore, since the group may not have been created in the test
        // or already deleted
      }
      
      try {
        identityService.deleteUser("testuser");
      } catch(Throwable ignore) {
        // Ignore, since the group may not have been created in the test
        // or already deleted
      }
    }
  }
  
  public void testCreateMembershipAlreadyExisting() throws Exception {
    try {
      Group testGroup = identityService.newGroup("testgroup");
      testGroup.setName("Test group");
      testGroup.setType("Test type");
      identityService.saveGroup(testGroup);
      
      User testUser = identityService.newUser("testuser");
      identityService.saveUser(testUser);
      
      identityService.createMembership("testuser", "testgroup");
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP_MEMBERSHIP_COLLECTION, "testgroup"));
      
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("userId", "testuser");
      
      try {
        client.post(requestNode);
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_CONFLICT, expected.getStatus());
        assertEquals("User 'testuser' is already part of group 'testgroup'.", expected.getStatus().getDescription());
      }
    } finally {
      try {
        identityService.deleteGroup("testgroup");
      } catch(Throwable ignore) {
        // Ignore, since the group may not have been created in the test
        // or already deleted
      }
      
      try {
        identityService.deleteUser("testuser");
      } catch(Throwable ignore) {
        // Ignore, since the group may not have been created in the test
        // or already deleted
      }
    }
  }
  
  public void testDeleteMembership() throws Exception {
    try {
      Group testGroup = identityService.newGroup("testgroup");
      testGroup.setName("Test group");
      testGroup.setType("Test type");
      identityService.saveGroup(testGroup);
      
      User testUser = identityService.newUser("testuser");
      identityService.saveUser(testUser);
      
      identityService.createMembership("testuser", "testgroup");
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_GROUP_MEMBERSHIP, "testgroup", "testuser"));
      
      Representation response = client.delete();
      assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      assertEquals(0, response.getSize());
      
      // Check if membership is actually deleted
      assertNull(identityService.createUserQuery().memberOfGroup("testgroup").singleResult());
    } finally {
      try {
        identityService.deleteGroup("testgroup");
      } catch(Throwable ignore) {
        // Ignore, since the group may not have been created in the test
        // or already deleted
      }
      
      try {
        identityService.deleteUser("testuser");
      } catch(Throwable ignore) {
        // Ignore, since the group may not have been created in the test
        // or already deleted
      }
    }
  }
  
  /**
   * Test delete membership that is no member in the group.
   */
  public void testDeleteMembershipNoMember() throws Exception {
    try {
      Group testGroup = identityService.newGroup("testgroup");
      testGroup.setName("Test group");
      testGroup.setType("Test type");
      identityService.saveGroup(testGroup);
      
      User testUser = identityService.newUser("testuser");
      identityService.saveUser(testUser);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
              RestUrls.URL_GROUP_MEMBERSHIP, "testgroup", "testuser"));
      
      try {
        client.delete();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("User 'testuser' is not part of group 'testgroup'.", expected.getStatus().getDescription());
      }
      
    } finally {
      try {
        identityService.deleteGroup("testgroup");
      } catch(Throwable ignore) {
        // Ignore, since the group may not have been created in the test
        // or already deleted
      }
      
      try {
        identityService.deleteUser("testuser");
      } catch(Throwable ignore) {
        // Ignore, since the group may not have been created in the test
        // or already deleted
      }
    }
  }
  
  /**
   * Test deleting member from an unexisting group.
   */
  public void testDeleteMemberfromUnexistingGroup() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(
            RestUrls.URL_GROUP_MEMBERSHIP, "unexisting", "kermit"));
    try {
      client.delete();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a group with id 'unexisting'.", expected.getStatus().getDescription());
    }
  }
  
   /**
   * Test adding member to an unexisting group.
   */
  public void testAddMemberToUnexistingGroup() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP_MEMBERSHIP_COLLECTION, "unexisting"));
    
    try {
      client.post(objectMapper.createObjectNode());
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a group with id 'unexisting'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test adding member to a group, without specifying userId
   */
  public void testAddMemberNoUserId() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_GROUP_MEMBERSHIP_COLLECTION, "admin"));
    
    try {
      client.post(objectMapper.createObjectNode());
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("UserId cannot be null.", expected.getStatus().getDescription());
    }
  }
}
