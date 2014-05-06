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

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.BaseRestTestCase;
import org.activiti.rest.service.HttpMultipartRepresentation;
import org.activiti.rest.service.api.RestUrls;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;


/**
 * @author Frederik Heremans
 */
public class UserPictureResourceTest extends BaseRestTestCase {

  /**
   * Test getting the picture for a user.
   */
  public void testGetUserPicture() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      MediaType mediaType = MediaType.IMAGE_PNG;
      // Create picture for user
      Picture thePicture = new Picture("this is the picture raw byte stream".getBytes(), mediaType.toString());
      identityService.setUserPicture(newUser.getId(), thePicture);
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_PICTURE,
              newUser.getId()));
      Representation response = client.get();
      assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
      assertEquals("this is the picture raw byte stream", response.getText());
      
      // Check if media-type is correct
      String typeFromResponse = getMediaType(client);
      assertEquals(mediaType.toString(), typeFromResponse);
      
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  /**
   * Test getting the picture for an unexisting user.
   */
  public void testGetPictureForUnexistingUser() throws Exception {
    ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_PICTURE, "unexisting"));
    
    try {
      client.get();
      fail("Exception expected");
    } catch(ResourceException expected) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
      assertEquals("Could not find a user with id 'unexisting'.", expected.getStatus().getDescription());
    }
  }
  
  /**
   * Test getting the picture for a user who doesn't have a îcture set
   */
  public void testGetPictureForUserWithoutPicture() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_PICTURE,
              newUser.getId()));
      try {
        client.get();
        fail("Exception expected");
      } catch(ResourceException expected) {
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, expected.getStatus());
        assertEquals("The user with id '" + newUser.getId() + "' does not have a picture.", expected.getStatus().getDescription());
      }
      
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  public void testUpdatePicture() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_PICTURE,
              newUser.getId()));
      
      HttpMultipartRepresentation request = new HttpMultipartRepresentation("myPicture.png", 
              new ByteArrayInputStream("this is the picture raw byte stream".getBytes())); 
      
      Representation response = client.put(request);
      assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      assertNotNull(response);
      
      Picture picture = ActivitiUtil.getIdentityService().getUserPicture(newUser.getId());
      assertNotNull(picture);
      assertEquals(MediaType.IMAGE_JPEG.toString(), picture.getMimeType());
      assertEquals("this is the picture raw byte stream", new String(picture.getBytes()));
      
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
  public void testUpdatePictureWithCustomMimeType() throws Exception {
    User savedUser = null;
    try {
      User newUser = identityService.newUser("testuser");
      newUser.setFirstName("Fred");
      newUser.setLastName("McDonald");
      newUser.setEmail("no-reply@activiti.org");
      identityService.saveUser(newUser);
      savedUser = newUser;
      
      ClientResource client = getAuthenticatedClient(RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_PICTURE,
              newUser.getId()));
      
      Map<String, String> additionalFields = new HashMap<String, String>();
      additionalFields.put("mimeType", MediaType.IMAGE_PNG.toString());
      HttpMultipartRepresentation request = new HttpMultipartRepresentation("myPicture.png", 
              new ByteArrayInputStream("this is the picture raw byte stream".getBytes()), additionalFields); 
      
      Representation response = client.put(request);
      assertEquals(Status.SUCCESS_NO_CONTENT, client.getResponse().getStatus());
      assertNotNull(response);
      
      Picture picture = ActivitiUtil.getIdentityService().getUserPicture(newUser.getId());
      assertNotNull(picture);
      assertEquals(MediaType.IMAGE_PNG.toString(), picture.getMimeType());
      assertEquals("this is the picture raw byte stream", new String(picture.getBytes()));
      
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
}
