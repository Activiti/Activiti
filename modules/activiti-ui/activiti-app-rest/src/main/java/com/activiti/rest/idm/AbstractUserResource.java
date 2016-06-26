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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.common.ImageUpload;
import com.activiti.domain.idm.User;
import com.activiti.model.idm.UserActionRepresentation;
import com.activiti.model.idm.UserRepresentation;
import com.activiti.repository.common.ImageUploadRepository;
import com.activiti.repository.idm.UserRepository;
import com.activiti.rest.util.ImageUploadUtil;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.UserService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.ConflictingRequestException;
import com.activiti.service.exception.InternalServerErrorException;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.exception.NotPermittedException;

/**
 * REST controller for managing users.
 */
@RestController
public class AbstractUserResource {
	
	private static final int MAX_RECENT_USERS = 50;
	
    private static final String USER_ALREADY_REGISTERED = "ACCOUNT.SIGNUP.ERROR.ALREADY-REGISTERED";

    private static final String UNEXISTING_USER_MESSAGE_KEY = "ACCOUNT.RESET-PASSWORD-REQUEST.ERROR.UNEXISTING-USER";

    private final Logger log = LoggerFactory.getLogger(AbstractUserResource.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ImageUploadRepository imageUploadRepository;
    
    @Autowired
    private UserService userService;
    
    public UserRepresentation getUser(Long userId, HttpServletResponse response) {
        
        User user = userService.findUser(userId);
        
        if (user == null) {
            throw new NotFoundException("User with id: " + userId + " does not exist or is inactive");
        }
        
        if(!user.getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new NotPermittedException("Can only get user details for authenticated user");
        }
        
        return new UserRepresentation(user);
    }
    
    public UserRepresentation updateUser(Long userId, UserRepresentation userRequest, HttpServletResponse response) {
        
        User user = userService.findUser(userId);
        
        if (user == null) {
            throw new NotFoundException("User with id: " + userId + " does not exist or is inactive");
        }
        
        if(!user.getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new NotPermittedException("Can only update user for authenticated user");
        }
        
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setCompany(userRequest.getCompany());
        
        userRepository.save(user);
        
        return new UserRepresentation(user);
    }
    
    public void executeAction(Long userId, UserActionRepresentation actionRequest, HttpServletResponse response) {
        
        User user = userService.findUser(userId);
        
        if (user == null) {
            throw new NotFoundException("User with id: " + userId + " does not exist or is inactive");
        }
        
        if(!user.getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new NotPermittedException("Can only update user for authenticated user");
        }
        
        if(UserActionRepresentation.ACTION_UPDATE_PASSWORD.equals(actionRequest.getAction())) {
            if(actionRequest.getNewPassword() == null || actionRequest.getOldPassword() == null) {
                throw new BadRequestException("Both old and new password are required");
            }
            boolean changed = userService.changePassword(userId, actionRequest.getOldPassword(), actionRequest.getNewPassword());
            
            if(!changed) {
                throw new ConflictingRequestException("Old password is incorrect");
            }
        } else {
           throw new BadRequestException("Unknown user action: " + actionRequest.getAction()); 
        }
    }
    
    public void registerUser(UserRepresentation signup, HttpServletRequest request) {
        
    	  // Create a new user, leaving status empty. Appropriate status will be set for new users and actions will be performed if needed.
        User user = userService.createNewUser(signup.getEmail(), signup.getFirstName(), signup.getLastName(), signup.getPassword(), signup.getCompany());
        
        if (user == null) {
        	
        	// Check if there is already a user with the same email
        	if (signup.getEmail() != null && userRepository.findByEmail(signup.getEmail()) != null) {
        		throw new ConflictingRequestException("User already registered", USER_ALREADY_REGISTERED);
        	} else {
        		throw new BadRequestException("Could not create user: please verify the parameters used to create this user");
        	}
        }
        
    }
    
    public void getProfilePicture(HttpServletResponse response, Long userId) {
        User currentUser = userService.getUser(SecurityUtils.getCurrentUserId(), false);
        User user = userService.getUser(userId, false);

        ImageUpload imageUpload = null;
        if (user.getPictureImageId() != null) {
            imageUpload = imageUploadRepository.findOne(user.getPictureImageId());
        }
        
        try {
            ImageUploadUtil.writeImageUploadToResponse(response, imageUpload, false);
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not get image " + user.getPictureImageId(), e);
        }
        
    }
    
}
