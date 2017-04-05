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
package org.activiti.app.rest.idm;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.activiti.app.model.idm.ChangePasswordRepresentation;
import org.activiti.app.model.idm.GroupRepresentation;
import org.activiti.app.model.idm.UserRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.InternalServerErrorException;
import org.activiti.app.service.exception.NotFoundException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.util.IoUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
@RestController
@RequestMapping(value = "/rest/admin")
public class IdmProfileResource {
  
  @Autowired
  protected IdentityService identityService;

  @RequestMapping(value = "/profile", method = RequestMethod.GET, produces = "application/json")
  public UserRepresentation getProfile() {
    User user = SecurityUtils.getCurrentActivitiAppUser().getUserObject();
    
    UserRepresentation userRepresentation = new UserRepresentation(user);
    
    List<Group> groups = identityService.createGroupQuery().groupMember(user.getId()).list();
    for (Group group : groups) {
      userRepresentation.getGroups().add(new GroupRepresentation(group));
    }
    
    return userRepresentation;
  }

  @RequestMapping(value = "/profile", method = RequestMethod.POST, produces = "application/json")
  public UserRepresentation updateProfile(@RequestBody UserRepresentation userRepresentation) {
    User currentUser = SecurityUtils.getCurrentUserObject();

    // If user is not externally managed, we need the email address for login, so an empty email is not allowed
    if (StringUtils.isEmpty(userRepresentation.getEmail())) {
      throw new BadRequestException("Empty email is not allowed");
    }
    
    User user = identityService.createUserQuery().userId(currentUser.getId()).singleResult();
    user.setFirstName(userRepresentation.getFirstName());
    user.setLastName(userRepresentation.getLastName());
    user.setEmail(userRepresentation.getEmail());
    identityService.saveUser(user);
    return new UserRepresentation(user);
  }
  
  @ResponseStatus(value = HttpStatus.OK)
  @RequestMapping(value = "/profile-password", method = RequestMethod.POST, produces = "application/json")
  public void changePassword(@RequestBody ChangePasswordRepresentation changePasswordRepresentation) {
    User user = identityService.createUserQuery().userId(SecurityUtils.getCurrentUserId()).singleResult();
    if (!user.getPassword().equals(changePasswordRepresentation.getOriginalPassword())) {
      throw new NotFoundException();
    }
    user.setPassword(changePasswordRepresentation.getNewPassword());
    identityService.saveUser(user);
  }
  
  @RequestMapping(value = "/profile-picture", method = RequestMethod.GET)
  public void getProfilePicture(HttpServletResponse response) {
    try {
      Picture picture = identityService.getUserPicture(SecurityUtils.getCurrentUserId());
      if(picture==null){
    	  try{
    		  byte[] pictureBytes = IoUtil.readInputStream(this.getClass().getClassLoader().getResourceAsStream("activiti-logo.png"), "default-logo");
    		  picture=new Picture(pictureBytes,"image/png");
    	  }
    	  catch (Exception e) {
    		  throw new InternalServerErrorException("Could not find default tenant logo");
    	  }
      }
      response.setContentType(picture.getMimeType());
      ServletOutputStream servletOutputStream = response.getOutputStream();
      BufferedInputStream in = new BufferedInputStream(  new ByteArrayInputStream(picture.getBytes()));
  
      byte[] buffer = new byte[32384];
      while (true) {
        int count = in.read(buffer);
        if (count == -1)
          break;
        servletOutputStream.write(buffer, 0, count);
      }
  
      // Flush and close stream
      servletOutputStream.flush();
      servletOutputStream.close();
    } catch (Exception e) {
      throw new InternalServerErrorException("Could not get profile picture", e);
    }
  }

  @ResponseStatus(value = HttpStatus.OK)
  @RequestMapping(value = "/profile-picture", method = RequestMethod.POST, produces = "application/json")
  public void uploadProfilePicture(@RequestParam("file") MultipartFile file) {
    Picture picture = null;
    try {
      picture = new Picture(file.getBytes(), file.getContentType());
    } catch (IOException e) {
      throw new InternalServerErrorException(e.getMessage(), e);
    }
    identityService.setUserPicture(SecurityUtils.getCurrentUserId(), picture);
  }
  
}
