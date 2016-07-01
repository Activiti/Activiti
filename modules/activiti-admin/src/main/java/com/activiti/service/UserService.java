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
package com.activiti.service;

import com.activiti.domain.Authority;
import com.activiti.repository.AuthorityRepository;
import com.activiti.web.rest.dto.AccountRepresentation;
import com.activiti.web.rest.dto.UserRepresentation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.User;
import com.activiti.repository.UserRepository;
import com.activiti.service.activiti.exception.ActivitiServiceException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected AuthorityRepository authorityRepository;
    
    @Autowired
    protected Environment env;
    
    /**
     * Creates admin user with the required role
     * 
     * @throws IllegalArgumentException when login and/or password is empty or null.
     * @returns the new user. Returns null, when a user with the given login already exists.
     */
    public User createAdminUser(String login, String password, String firstName, String lastName, String email) {
    	
    	return createUser(login, password, firstName, lastName, email, Authority.ROLE_ADMIN);
    }
    

    public User createUser(UserRepresentation userRepresentation) {
        
        String authority = (userRepresentation.getAdminUser() != null && userRepresentation.getAdminUser() == true) ? Authority.ROLE_ADMIN : Authority.ROLE_USER;
        
        return createUser(userRepresentation.getLogin(), userRepresentation.getPassword(), userRepresentation.getFirstName(), 
                userRepresentation.getLastName(), userRepresentation.getEmail(), authority);
    }

    /**
     * @throws IllegalArgumentException when login and/or password is empty or null.
     * @returns the new user. Returns null, when a user with the given login already exists.
     */
    public User createUser(String login, String password, String firstName, String lastName, String email, String ... authorities) {

    	if(StringUtils.isEmpty(login) || StringUtils.isEmpty(password)) {
    		throw new IllegalArgumentException("Both username and password are required");
    	}

    	User existing = userRepository.findOne(login);

    	if(existing == null) {
    		User user = new User();

            // Regular props
    		user.setPassword(passwordEncoder.encode(password));
    		user.setFirstName(firstName);
    		user.setLastName(lastName);
    		user.setEmail(email);
    		user.setLogin(login);

            // Authorities
            if (authorities != null && authorities.length > 0) {
                for (String authority : authorities) {
                    Authority authorityEntity = authorityRepository.findOne(authority);
                    if (authorityEntity != null) {
                        if (user.getAuthorities() == null) {
                            user.setAuthorities(new HashSet<Authority>());
                        }
                        user.getAuthorities().add(authorityEntity);
                    } else {
                        log.warn("Programmatic error: could not find authority entity for '" + authority + "'");
                    }
                }
            }

    		userRepository.save(user);
            userRepository.flush();
    		return user;
    	} else {
	    	// User already exists
	    	return null;
	    }
    }


    public void changePassword(String login, String oldPassword, String newPassword) {
        User currentUser = getUserFromDB(login);
        if (passwordEncoder.matches(oldPassword, currentUser.getPassword()) == false) {
        	throw new ActivitiServiceException("Old password is not correct");
        }
        String newEncryptedPassword = passwordEncoder.encode(newPassword);
        currentUser.setPassword(newEncryptedPassword);
        userRepository.save(currentUser);
        log.debug("Changed password for User: {}", currentUser);
    }
    
    public void updateUser(String login, String firstName, String lastName, String email) {
        User user = getUserFromDB(login);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        userRepository.save(user);
        userRepository.flush();
    }

    public void deleteUser(String login) {
        userRepository.delete(login);
    }

    public List<UserRepresentation> getAllUsers() {
        List<User> users = userRepository.findAll(new Sort("login"));
        List<UserRepresentation> result = new ArrayList<UserRepresentation>(users.size());
        for (User user : users) {
            result.add(new UserRepresentation(user));
        }
        return result;
    }
    
    public UserRepresentation getUserRepresentation(String login) {
        User user = userRepository.findOne(login);
        if (user != null) {
            return new UserRepresentation(user);
        }
        return null;
    }
    
    public AccountRepresentation getAccountRepresentation(String login) {
        User user = userRepository.findOne(login);
        if (user != null) {
            return new AccountRepresentation(user);
        }
        return null;
    }
    
    public User getUser(String login) {
        return userRepository.findOne(login);
    }
    
    protected User getUserFromDB(String login) {
        User user = userRepository.findOne(login);
        if (user == null) {
            throw new ActivitiServiceException("User with login '" + login +"' not found");
        }
        return user;
    }
}
