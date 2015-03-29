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

import java.io.ByteArrayOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author Frederik Heremans
 */
@RestController
public class UserPictureResource extends BaseUserResource {

  @RequestMapping(value="/identity/users/{userId}/picture", method = RequestMethod.GET)
  public ResponseEntity<byte[]> getUserPicture(@PathVariable String userId, HttpServletRequest request, HttpServletResponse response) {
    User user = getUserFromRequest(userId);
    Picture userPicture = identityService.getUserPicture(user.getId());
    
    if (userPicture == null) {
      throw new ActivitiObjectNotFoundException("The user with id '" + user.getId() + "' does not have a picture.", Picture.class);
    }
    
    HttpHeaders responseHeaders = new HttpHeaders();
    if (userPicture.getMimeType() != null) {
      responseHeaders.set("Content-Type", userPicture.getMimeType());
    }else{
      responseHeaders.set("Content-Type", "image/jpeg");
    }
    
    try {
      return new ResponseEntity<byte[]>(IOUtils.toByteArray(userPicture.getInputStream()), responseHeaders, HttpStatus.OK);
    } catch (Exception e) {
      throw new ActivitiException("Error exporting picture: " + e.getMessage(), e);
    }
  }
  
  @RequestMapping(value="/identity/users/{userId}/picture", method = RequestMethod.PUT)
  public void updateUserPicture(@PathVariable String userId, HttpServletRequest request, HttpServletResponse response) {
    User user = getUserFromRequest(userId);
    
    if (request instanceof MultipartHttpServletRequest == false) {
      throw new ActivitiIllegalArgumentException("Multipart request is required");
    }
    
    MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
    
    if (multipartRequest.getFileMap().size() == 0) {
      throw new ActivitiIllegalArgumentException("Multipart request with file content is required");
    }
    
    MultipartFile file = multipartRequest.getFileMap().values().iterator().next();
    
    try {
      String mimeType = file.getContentType();
      int size = ((Long) file.getSize()).intValue();
      
      // Copy file-body in a bytearray as the engine requires this
      ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream(size);
      IOUtils.copy(file.getInputStream(), bytesOutput);
      
      Picture newPicture = new Picture(bytesOutput.toByteArray(), mimeType);
      identityService.setUserPicture(user.getId(), newPicture);
      
      response.setStatus(HttpStatus.NO_CONTENT.value());
      
    } catch (Exception e) {
      throw new ActivitiException("Error while reading uploaded file: " + e.getMessage(), e);
    }
  }
}
