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
package com.activiti.web.rest;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import com.activiti.web.rest.dto.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.service.UserService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * REST controller for managing users.
 */
@RestController
public class UserResource {

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    @Autowired
    protected UserService userService;
    
    @Autowired
    protected Environment env;

    /**
     * GET  /rest/users/:login -> get the "login" user.
     */
    @RequestMapping(value = "/rest/users/{login}",
            method = RequestMethod.GET,
            produces = "application/json")
    public UserRepresentation getUser(@PathVariable String login, HttpServletResponse response) {
        log.debug("REST request to get User : {}", login);

        UserRepresentation user = userService.getUserRepresentation(login);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return user;
    }

    /**
     * PUT  /rest/users/:login -> update the user.
     */
    @RequestMapping(value = "/rest/users/{login}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateUser(@PathVariable String login, @RequestBody UserRepresentation userRepresentation) {
        log.debug("REST request to get User : {}", login);

        try {
      	    userService.updateUser(login, userRepresentation.getFirstName(), userRepresentation.getLastName(), userRepresentation.getEmail());
      	} catch (ConstraintViolationException cv) {
            String message = "Invalid user details";
            if (cv.getConstraintViolations().size() > 0) {
                message = cv.getConstraintViolations().iterator().next().getMessage();
            }
            throw new BadRequestException(message);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * PUT  /rest/users/:login/change_password -> changes the user's password
     */
    @RequestMapping(value = "/rest/users/{login}/change-password", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    public void changePassword(@PathVariable String login, @RequestBody ObjectNode actionBody) {
        if (actionBody.get("oldPassword") == null || actionBody.get("oldPassword").isNull()) {
            throw new BadRequestException("oldPassword should not be empty");

        } else if (actionBody.get("newPassword") == null || actionBody.get("newPassword").isNull()) {
            throw new BadRequestException("newPassword should not be empty");

        } else {
        	try {
        		userService.changePassword(login, actionBody.get("oldPassword").asText(), actionBody.get("newPassword").asText());
        	} catch(ActivitiServiceException ase) {
        		throw new BadRequestException(ase.getMessage());
        	}
        }
    }

    /**
     * DELETE  /rest/users/:login -> delete the user.
     */
    @RequestMapping(value = "/rest/users/{login}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String login) {
        log.debug("REST request to delete User : {}", login);
        userService.deleteUser(login);
    }
}
