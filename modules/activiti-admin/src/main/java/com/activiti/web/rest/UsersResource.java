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

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.User;
import com.activiti.service.UserService;
import com.activiti.web.rest.dto.UserRepresentation;
import com.activiti.web.rest.exception.BadRequestException;
import com.activiti.web.rest.exception.ConflictException;

/**
 * REST controller for managing users.
 */
@RestController
public class UsersResource {

    private final Logger log = LoggerFactory.getLogger(UsersResource.class);

    @Autowired
    protected UserService userService;
    
    @Autowired
    protected Environment env;

    /**
     * GET  /rest/users -> get a list of users.
     */
    @RequestMapping(value = "/rest/users",
            method = RequestMethod.GET,
            produces = "application/json")
    public List<UserRepresentation> getUsers() {
        return userService.getAllUsers();
    }
    
    /**
     * POST  /rest/users -> create a new user.
     */
    @RequestMapping(value = "/rest/users", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void createUser(@RequestBody UserRepresentation userRepresentation) {
        log.debug("REST request to create a User : {}");

            // all users are admins
        userRepresentation.setIsAdmin(true);

        if (userRepresentation.getLogin() == null) {
            throw new BadRequestException("user login is required");
        }
        
        if (userRepresentation.getPassword() == null) {
            throw new BadRequestException("a password is required");
        }
        
        try {
            User result = userService.createUser(userRepresentation);
            if(result == null) {
                throw new ConflictException("User with login '" + userRepresentation.getLogin() + "' already exists.");
            }
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException(iae.getMessage());
        } catch (ConstraintViolationException cv) {
            String message = "Invalid user details";
            if (cv.getConstraintViolations().size() > 0) {
                message = cv.getConstraintViolations().iterator().next().getMessage();
            }
            throw new BadRequestException(message);
        }
        
    }
}
