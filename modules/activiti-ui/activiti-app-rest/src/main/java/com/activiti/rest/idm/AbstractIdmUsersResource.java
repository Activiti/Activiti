/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.rest.idm;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.activiti.domain.idm.User;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.idm.AbstractUserRepresentation;
import com.activiti.model.idm.BulkUserUpdateRepresentation;
import com.activiti.model.idm.LightUserRepresentation;
import com.activiti.model.idm.UserRepresentation;
import com.activiti.repository.idm.UserRepository;
import com.activiti.service.api.UserService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.ConflictingRequestException;

/**
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class AbstractIdmUsersResource {
    
    private static final int MAX_USER_SIZE = 100;

    @Inject
    protected UserService userService;
    
    @Inject
    protected UserRepository userRepository;
    
    public ResultListDataRepresentation getUsers(String filter, String sort, String company,
            Integer start, Integer page, Integer size, Long groupId, boolean summary) {

    	// Query
        ResultListDataRepresentation result = new ResultListDataRepresentation();

        // Determine sort
        Sort sortObject = null;
        if (sort != null) {
            if ("createdAsc".equals(sort)) {
                sortObject = new Sort(Direction.ASC, "created");
            } else if("createdDesc".equals(sort)) {
                sortObject = new Sort(Direction.DESC, "created");
            } else if("emailAsc".equals(sort)) {
                sortObject = new Sort(Direction.ASC, "email");
            } else if("emailDesc".equals(sort)) {
                sortObject = new Sort(Direction.DESC, "email");
            } else if("nameAsc".equals(sort)) {
                sortObject = new Sort(Direction.ASC, "firstName", "lastName");
            } else if("nameDesc".equals(sort)) {
                sortObject = new Sort(Direction.DESC, "firstName", "lastName");
            }
        } else {
            sortObject = new Sort(Direction.ASC, "created");
        }
        
        /*
         * There are two params for paging:
         * page/size is used by the public api and has precedence.
         * start is there for historical reasons, and is used by the UI.
         */
        Pageable pageable = null;
        if (page != null && page >= 0) {
        	pageable = new PageRequest(page, (size != null && size > 0) ? size : MAX_USER_SIZE, sortObject);
        } else if (start != null && start >= 0) {
        	pageable = new PageRequest(start/MAX_USER_SIZE, MAX_USER_SIZE, sortObject);
        } else {
        	pageable = new PageRequest(0, MAX_USER_SIZE, sortObject);
        }
        
        List<User> users = userService.findUsers(filter, true, null, company, groupId, pageable);
        Long totalCount = userService.countUsers(filter, true, null, company, groupId);
        result.setTotal(totalCount.intValue());
        result.setStart(start);
        
        if (users.size() > 0) {
            List<AbstractUserRepresentation> data = new ArrayList<AbstractUserRepresentation>();
            for (User sharedWith : users) {
                if (summary) {
                    data.add(new LightUserRepresentation(sharedWith));
                } else {
                    data.add(new UserRepresentation(sharedWith, true, false));
                }
            }
            result.setSize(data.size());
            result.setData(data);
        } else {
            result.setData(new ArrayList<AbstractUserRepresentation>());
            result.setSize(0);
        }
        return result;
    }
    
    public ResultListDataRepresentation getUsers(String filter, String sort, String company, Integer start, Integer page, Integer size, Long groupId) {
        return getUsers(filter, sort, company, start, page, size, groupId, false);
    }
    
    public AbstractUserRepresentation getUser(Long userId, boolean summary) {
        User user = userService.findUser(userId);
        
        if (summary) {
            return new LightUserRepresentation(user);
        } else {
            return new UserRepresentation(user, true, false);
        }
    }

    public void updateUserDetails(Long userId, UserRepresentation userRepresentation) {
    	User user = userService.findUser(userId);
    	
    	try {
    		userService.updateUser(userId, userRepresentation.getEmail(), 
	    			userRepresentation.getFirstName(), 
	    			userRepresentation.getLastName(), 
	    			userRepresentation.getCompany());
    	} catch (IllegalStateException e) {
    		throw new ConflictingRequestException("Email already registered", "ACCOUNT.SIGNUP.ERROR.ALREADY-REGISTERED");
    	}
    }
    
    public void bulkUpdateUsers(BulkUserUpdateRepresentation update) {
    	
    	// Param check
    	
        if(update.getUsers() == null || update.getUsers().size() == 0) {
            throw new BadRequestException("Users to update are required");
        }

        // Fetch all users
        List<User> users = new ArrayList<User>();
        User user = null; 
        for(Long userId : update.getUsers()) {
            user = userRepository.findOne(userId);
            if(user == null) {
                throw new BadRequestException("User with id: " + userId + " not found.");
            }
            users.add(user);
        }
        
        // Update the users one by one
        for(User userToUpdate : users) {
            if (update.getPassword() != null && update.getPassword().length() > 0) {
            	userService.changePassword(userToUpdate.getId(), update.getPassword());
            }
        }
    }
    
    public UserRepresentation createNewUser(UserRepresentation userRepresentation) {
    	
        if(StringUtils.isBlank(userRepresentation.getEmail()) ||
        		StringUtils.isBlank(userRepresentation.getPassword()) || 
        		StringUtils.isBlank(userRepresentation.getLastName())) {
            throw new BadRequestException("Email, password and last name are required");
        }

        User newUser = userService.createNewUser(userRepresentation.getEmail(),
        		userRepresentation.getFirstName(), 
        		userRepresentation.getLastName(), 
        		userRepresentation.getPassword(), 
                userRepresentation.getCompany());
        
        if (newUser == null) {
        	
        	// Check if there is already a user with the same email
        	if (userRepresentation.getEmail() != null && userRepository.findByEmail(userRepresentation.getEmail()) != null) {
        		throw new ConflictingRequestException("User already registered", "ACCOUNT.SIGNUP.ERROR.ALREADY-REGISTERED");
        	} else {
        		throw new BadRequestException("Could not create user: please verify the parameters used to create this user");
        	}
        }
        
        return new UserRepresentation(newUser);
    }
}
