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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.idm.UserActionRepresentation;
import com.activiti.model.idm.UserRepresentation;
import com.codahale.metrics.annotation.Timed;

/**
 * REST controller for managing users.
 */
@RestController
public class UserResource extends AbstractUserResource {
	
    @RequestMapping(value = "/rest/users/{userId}",
            method = RequestMethod.GET,
            produces = "application/json")
    @Timed
    public UserRepresentation getUser(@PathVariable Long userId, HttpServletResponse response) {
       return super.getUser(userId, response);
    }
    
    @RequestMapping(value = "/rest/users/{userId}",
            method = RequestMethod.PUT,
            produces = "application/json")
    @Timed
    public UserRepresentation updateUser(@PathVariable Long userId, @RequestBody UserRepresentation userRequest, HttpServletResponse response) {
        return super.updateUser(userId, userRequest, response);
    }

    @RequestMapping(value = "/rest/users/{userId}",
            method = RequestMethod.POST,
            produces = "application/json")
    @Timed
    public void executeAction(@PathVariable Long userId, @RequestBody UserActionRepresentation actionRequest, HttpServletResponse response) {
        super.executeAction(userId, actionRequest, response);
    }
    
    @RequestMapping(value = "/rest/idm/signups", method = RequestMethod.POST)
    @Timed
    public void registerUser(@RequestBody UserRepresentation signup, HttpServletRequest request) {
    	super.registerUser(signup, request);
    }
    
    @Timed
    @RequestMapping(value="/rest/users/{userId}/picture", method = RequestMethod.GET)
    public void getProfilePicture(HttpServletResponse response, @PathVariable("userId") Long userId) {
    	super.getProfilePicture(response, userId);
    }
    
}
