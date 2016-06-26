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
package com.activiti.conf;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;
import com.activiti.repository.editor.ModelRepository;
import com.activiti.repository.idm.GroupRepository;
import com.activiti.repository.idm.UserRepository;
import com.activiti.service.api.DeploymentService;
import com.activiti.service.api.UserService;
import com.activiti.service.editor.ModelInternalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;

/**
 * Responsible for executing all action required after booting up the Spring container.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@Component
public class Bootstrapper implements ApplicationListener<ContextRefreshedEvent>{

    private final Logger log = LoggerFactory.getLogger(Bootstrapper.class);

    @Autowired
    private TransactionTemplate transactionTemplate;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ModelInternalService modelService;
    
    @Autowired
    private DeploymentService deploymentService;
    
    @Autowired
    private ModelRepository modelRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private Environment env;
    
    @Autowired
    private PasswordEncoder encoder;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
    	if (event.getApplicationContext().getParent() == null) { // Using Spring MVC, there are multiple child contexts. We only care about the root
    		
    		// First create the default IDM entities
    		createDefaultAdmin();
    	}
     }
    
	protected void createDefaultAdmin() {
		// Need to be done in a separate TX, otherwise the LDAP sync won't see the created groups/users
		// Can't use @Transactional here, cause it seems not to be applied as wanted
		transactionTemplate.execute(new TransactionCallback<Void>() {
			
			@Override
			public Void doInTransaction(TransactionStatus status) {
				if (userService.getUserCount() == 0) {
			        log.info("No users found, initializing default entities");
                    User user = initializeSuperUser();
                    initializeSuperUserGroups(user);
			    }
			    return null;
			}
			
		});
    }

    protected User initializeSuperUser() {
        String adminPassword = env.getRequiredProperty("admin.passwordHash");
        String adminLastname = env.getRequiredProperty("admin.lastname");
        String adminEmail = env.getRequiredProperty("admin.email");

        User admin = userService.createNewUserHashedPassword(adminEmail, null, adminLastname, adminPassword, null);
        return admin;
    }
    
    protected void initializeSuperUserGroups(User superUser) {
    	String superUserGroupName = env.getRequiredProperty("admin.group");
    	Group group = new Group();
    	group.setName(superUserGroupName);
    	group.setUsers(ImmutableSet.<User> of(superUser));
    	group.setLastUpdate(new Date());

    	groupRepository.save(group);
    }
}