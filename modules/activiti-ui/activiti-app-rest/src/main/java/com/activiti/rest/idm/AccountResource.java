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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.idm.UserRepresentation;
import com.activiti.service.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * REST controller for managing the current user's account.
 * 
 * @author Joram Barrez
 */
@RestController
public class AccountResource extends AbstractAccountResource {
	
	@Autowired
	private ObjectMapper objectMapper;

    /**
     * GET  /rest/authenticate -> check if the user is authenticated, and return its full name.
     */
    @RequestMapping(value = "/rest/authenticate", method = RequestMethod.GET, produces = {"application/json"})
    public ObjectNode isAuthenticated(HttpServletRequest request) {
        String user = request.getRemoteUser();
        
        if(user == null) {
            throw new UnauthorizedException("Request did not contain valid authorization");
        }
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("login", user);
        return result;
    }

    /**
     * GET  /rest/account -> get the current user.
     */
    @RequestMapping(value = "/rest/account", method = RequestMethod.GET, produces = "application/json")
    public UserRepresentation getAccount(HttpServletResponse response, @RequestParam(value="includeApps", required=false) Boolean includeApps) {
    	return super.getAccount(response, includeApps);
    }
    
}
