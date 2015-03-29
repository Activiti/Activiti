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
import org.activiti.rest.service.BaseSpringRestTestCase;
import org.activiti.rest.service.HttpMultipartHelper;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.springframework.http.MediaType;


/**
 * @author Frederik Heremans
 */
public class UserPictureResourceTest extends BaseSpringRestTestCase {

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
      
      // Create picture for user
      Picture thePicture = new Picture("this is the picture raw byte stream".getBytes(), "image/png");
      identityService.setUserPicture(newUser.getId(), thePicture);
      
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_PICTURE, newUser.getId())), HttpStatus.SC_OK);
      
      assertEquals("this is the picture raw byte stream", IOUtils.toString(response.getEntity().getContent()));
      
      // Check if media-type is correct
      assertEquals("image/png", response.getEntity().getContentType().getValue());
      closeResponse(response);
      
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
    closeResponse(executeRequest(new HttpGet(SERVER_URL_PREFIX + 
        RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_PICTURE, "unexisting")), HttpStatus.SC_NOT_FOUND));
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
      
      CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_PICTURE, newUser.getId())), HttpStatus.SC_NOT_FOUND);
      
      // response content type application/json;charset=UTF-8
      assertEquals("application/json", response.getEntity().getContentType().getValue().split(";")[0]);
      closeResponse(response);
      
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
      
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_PICTURE, newUser.getId()));
      httpPut.setEntity(HttpMultipartHelper.getMultiPartEntity("myPicture.png", "image/png",
              new ByteArrayInputStream("this is the picture raw byte stream".getBytes()), null));
      closeResponse(executeBinaryRequest(httpPut, HttpStatus.SC_NO_CONTENT));
      
      Picture picture = identityService.getUserPicture(newUser.getId());
      assertNotNull(picture);
      assertEquals("image/png", picture.getMimeType());
      assertEquals("this is the picture raw byte stream", new String(picture.getBytes()));
      
    } finally {
      
      // Delete user after test passes or fails
      if (savedUser != null) {
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
      
      Map<String, String> additionalFields = new HashMap<String, String>();
      additionalFields.put("mimeType", MediaType.IMAGE_PNG.toString());
      
      HttpPut httpPut = new HttpPut(SERVER_URL_PREFIX + 
          RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_PICTURE, newUser.getId()));
      httpPut.setEntity(HttpMultipartHelper.getMultiPartEntity("myPicture.png", "image/png",
              new ByteArrayInputStream("this is the picture raw byte stream".getBytes()), additionalFields));
      closeResponse(executeBinaryRequest(httpPut, HttpStatus.SC_NO_CONTENT));
      
      Picture picture = identityService.getUserPicture(newUser.getId());
      assertNotNull(picture);
      assertEquals("image/png", picture.getMimeType());
      assertEquals("this is the picture raw byte stream", new String(picture.getBytes()));
      
    } finally {
      
      // Delete user after test passes or fails
      if(savedUser != null) {
        identityService.deleteUser(savedUser.getId());
      }
    }
  }
  
}
