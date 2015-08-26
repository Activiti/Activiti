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

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.activiti.model.common.ImageUploadRepresentation;
import com.activiti.model.idm.ChangePasswordRepresentation;
import com.activiti.model.idm.UserRepresentation;
import com.codahale.metrics.annotation.Timed;

/**
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@RestController
@RequestMapping(value="/rest/admin")
public class IdmProfileResource extends AbstractIdmProfileResource {
    
    @Timed
    @RequestMapping(value="/profile", method = RequestMethod.GET, produces = "application/json")
    public UserRepresentation getProfile() {
    	return super.getProfile();
    }
    
    @Timed
    @RequestMapping(value="/profile", method = RequestMethod.POST, produces = "application/json")
    public UserRepresentation updateUser(@RequestBody UserRepresentation userRepresentation) {
    	return super.updateUser(userRepresentation);
    }
    
    @Timed
    @RequestMapping(value="/profile-picture", method = RequestMethod.GET)
    public void getProfilePicture(HttpServletResponse response) {
    	super.getProfilePicture(response);
    }
    
    @Timed
    @RequestMapping(value="/profile-picture", method = RequestMethod.POST, produces = "application/json")
	public ImageUploadRepresentation uploadProfilePicture(@RequestParam("file") MultipartFile file) {
		return super.uploadProfilePicture(file);
	}
    
    @Timed
    @RequestMapping(value="/profile-password", method = RequestMethod.POST, produces = "application/json")
    public void changePassword(@RequestBody ChangePasswordRepresentation changePasswordRepresentation) {
    	super.changePassword(changePasswordRepresentation);
    }
    
}
