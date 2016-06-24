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
package com.activiti.rest.idm;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.activiti.domain.common.ImageUpload;
import com.activiti.domain.idm.User;
import com.activiti.model.common.ImageUploadRepresentation;
import com.activiti.model.idm.ChangePasswordRepresentation;
import com.activiti.model.idm.UserRepresentation;
import com.activiti.repository.common.ImageUploadRepository;
import com.activiti.rest.util.ImageUploadUtil;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.GroupHierarchyCache;
import com.activiti.service.api.UserCache;
import com.activiti.service.api.UserCache.CachedUser;
import com.activiti.service.api.UserService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.ConflictingRequestException;
import com.activiti.service.exception.InternalServerErrorException;
import com.activiti.service.exception.NotFoundException;

/**
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class AbstractIdmProfileResource {
    
	private static final Logger logger = LoggerFactory.getLogger(AbstractIdmProfileResource.class);
	
	@Autowired
    private UserService userService;
    
	@Autowired
    private UserCache userCache;

	@Autowired
    private ImageUploadRepository imageUploadRepository;
    
    public UserRepresentation getProfile() {
    	CachedUser cachedUser = userCache.getUser(SecurityUtils.getCurrentUserId());
    	UserRepresentation userRepresentation = new UserRepresentation(cachedUser.getUser(), true, true);

    	return userRepresentation;
    }
    
    public UserRepresentation updateUser(UserRepresentation userRepresentation) {
    	
    	User currentUser = SecurityUtils.getCurrentUserObject();
    	
    	// If user is not externally managed, we need the email address for login, so an empty email is not allowed
    	if (StringUtils.isEmpty(userRepresentation.getEmail())) {
    		throw new BadRequestException("Empty email is not allowed");
    	}
    	
    	User user = null;
    	try {
    		 user = userService.updateUser(currentUser.getId(), 
    			userRepresentation.getEmail(), 
    			userRepresentation.getFirstName(), 
    			userRepresentation.getLastName(), 
    			userRepresentation.getCompany());
    	} catch (IllegalStateException e) {
    		throw new ConflictingRequestException("Email already registered", "ACCOUNT.SIGNUP.ERROR.ALREADY-REGISTERED");
    	}
    	
    	if (user == null) {
    		throw new NotFoundException();
    	}
    	return new UserRepresentation(user);
    }
    
    public void getProfilePicture(HttpServletResponse response) {
    	User user = userService.getUser(SecurityUtils.getCurrentUserId(), false);
    	
    	ImageUpload imageUpload = null;
		if (user.getPictureImageId() != null) {
			imageUpload = imageUploadRepository.findOne(user.getPictureImageId());
		}
    	
		try {
			ImageUploadUtil.writeImageUploadToResponse(response, imageUpload, true);
		} catch (IOException e) {
			logger.error("Could not get image " + user.getPictureImageId(), e);
			throw new InternalServerErrorException("Could not get image " + user.getPictureImageId());
		}
    	
    }
    
	public ImageUploadRepresentation uploadProfilePicture(MultipartFile file) {
		
        try {
            ImageUpload imageUpload = userService.updateUserPicture(file, SecurityUtils.getCurrentUserId());
            if (imageUpload == null) {
            	throw new NotFoundException();
            }
            return new ImageUploadRepresentation(imageUpload);
        } catch (Exception e) {
            logger.error("Error saving image " + file.getOriginalFilename(), e);
            throw new InternalServerErrorException("Error saving image " + file.getOriginalFilename());
        }
	}
    
    public void changePassword(ChangePasswordRepresentation changePasswordRepresentation) {
    	if (changePasswordRepresentation.getOldPassword() == null || changePasswordRepresentation.getOldPassword().length() == 0
    			|| changePasswordRepresentation.getNewPassword() == null || changePasswordRepresentation.getNewPassword().length() == 0) {
    		throw new BadRequestException("Invalid passwords");
    	}
    	
    	boolean success = userService.changePassword(SecurityUtils.getCurrentUserId(), 
    			changePasswordRepresentation.getOldPassword(), changePasswordRepresentation.getNewPassword());
    	if (!success) {
    		throw new BadRequestException("Invalid password");
    	}
    	
    }
    
}
