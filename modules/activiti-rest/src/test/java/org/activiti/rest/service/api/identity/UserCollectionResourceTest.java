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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.identity.User;
import org.activiti.engine.test.Deployment;
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
public class UserCollectionResourceTest extends BaseRestTestCase {

  /**
   * Test getting all users.
   */
  @Deployment
  public void testGetUsers() throws Exception {
    List<User> savedUsers = new ArrayList<User>();
    try {
      User user1 = identityService.newUser("testuser");
      user1.setFirstName("Fred");
      user1.setLastName("McDonald");
      user1.setEmail("no-reply@activiti.org");
      identityService.saveUser(user1);
      savedUsers.add(user1);
      
      User user2 = identityService.newUser("anotherUser");
      user2.setFirstName("Tijs");
      user2.setLastName("Barrez");
      user2.setEmail("no-reply@alfresco.org");
      identityService.saveUser(user2);
      savedUsers.add(user2);
      
      User user3 = identityService.createUserQuery().userId("kermit").singleResult();
      assertNotNull(user3);
      
      // Test filter-less
      String url = RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_COLLECTION);
      assertResultsPresentInDataResponse(url, user1.getId(), user2.getId(), user3.getId());
      
      // Test based on userId
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_COLLECTION) + "?id=testuser";
      assertResultsPresentInDataResponse(url, user1.getId());
      
      // Test based on firstName
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_COLLECTION) + "?firstName=Tijs";
      assertResultsPresentInDataResponse(url, user2.getId());
      
      // Test based on lastName
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_COLLECTION) + "?lastName=Barrez";
      assertResultsPresentInDataResponse(url, user2.getId());
      
      // Test based on email
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_COLLECTION) + "?email=no-reply@activiti.org";
      assertResultsPresentInDataResponse(url, user1.getId());
      
      // Test based on firstNameLike
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_COLLECTION) + "?firstNameLike=" + encode("%ij%");
      assertResultsPresentInDataResponse(url, user2.getId());
      
      // Test based on lastNameLike
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_COLLECTION) + "?lastNameLike=" + encode("%rez");
      assertResultsPresentInDataResponse(url, user2.getId());
      
      // Test based on emailLike
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_COLLECTION) + "?emailLike=" + encode("no-reply@activiti.org%");
      assertResultsPresentInDataResponse(url, user1.getId());
      
      // Test based on memberOfGroup
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_COLLECTION) + "?memberOfGroup=admin";
      assertResultsPresentInDataResponse(url, user3.getId());
      
      // Test based on potentialStarter
      String processDefinitionId = repositoryService.createProcessDefinitionQuery().processDefinitionKey("simpleProcess")
              .singleResult().getId();
      repositoryService.addCandidateStarterUser(processDefinitionId, "kermit");
     
      url = RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_COLLECTION) + "?potentialStarter=" + processDefinitionId;
      assertResultsPresentInDataResponse(url, user3.getId());
      
      
    } finally {
      
      // Delete user after test passes or fails
      if(savedUsers.size() > 0) {
        for(User user : savedUsers) {
          identityService.deleteUser(user.getId());
        }
      }
    }
  }
  
  public void testCreateUser() throws Exception {
    try {
      ObjectNode requestNode = objectMapper.createObjectNode();
      requestNode.put("id", "testuser");
      requestNode.put("firstName", "Frederik");
      requestNode.put("lastName", "Heremans");
      requestNode.put("password", "test");
      requestNode.put("email", "no-reply@activiti.org");
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_COLLECTION, "testuser"));
      Representation response = client.post(requestNode);
      assertEquals(Status.SUCCESS_CREATED, client.getResponse().getStatus());
      
      JsonNode responseNode = objectMapper.readTree(response.getStream());
      assertNotNull(responseNode);
      assertEquals("testuser", responseNode.get("id").textValue());
      assertEquals("Frederik", responseNode.get("firstName").textValue());
      assertEquals("Heremans", responseNode.get("lastName").textValue());
      assertEquals("no-reply@activiti.org", responseNode.get("email").textValue());
      assertTrue(responseNode.get("url").textValue().endsWith(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER, "testuser")));
      
      assertNotNull(identityService.createUserQuery().userId("testuser").singleResult());
    } finally {
      try {
        identityService.deleteUser("testuser");
      } catch(Throwable t) {
        // Ignore, user might not have been created by test
      }
    }
  }
  
  public void testCreateUserExceptions() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_COLLECTION, "unexisting"));
    
    // Create without ID
    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put("firstName", "Frederik");
    requestNode.put("lastName", "Heremans");
    requestNode.put("email", "no-reply@activiti.org");
    
    try {
      client.post(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, expected.getStatus());
      assertEquals("Id cannot be null.", expected.getStatus().getDescription());
    }
    
    // Create when user already exists
    // Create without ID
    requestNode = objectMapper.createObjectNode();
    requestNode.put("id", "kermit");
    requestNode.put("firstName", "Frederik");
    requestNode.put("lastName", "Heremans");
    requestNode.put("email", "no-reply@activiti.org");
    
    try {
      client.post(requestNode);
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_CONFLICT, expected.getStatus());
      assertEquals("A user with id 'kermit' already exists.", expected.getStatus().getDescription());
    }
  }
}
