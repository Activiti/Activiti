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
package com.activiti.rest.runtime;

import java.util.List;

import javax.inject.Inject;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.activiti.domain.idm.User;
import com.activiti.model.runtime.TaskRepresentation;
import com.activiti.rest.util.TaskUtil;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.UserCache.CachedUser;
import com.activiti.service.api.UserService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.exception.NotPermittedException;
import com.activiti.service.runtime.PermissionService;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractTaskActionResource extends AbstractTaskResource {
    
    private final Logger log = LoggerFactory.getLogger(AbstractTaskActionResource.class);

    @Inject
    private TaskService taskService;
    
    @Inject
    private PermissionService permissionService;
    
    @Inject
    private UserService userService;
    
    public void completeTask(String taskId) {
		User currentUser = SecurityUtils.getCurrentUserObject();
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		
		if (task == null) {
            throw new NotFoundException("Task with id: " + taskId + " does not exist");
        }
		
		if (!permissionService.isTaskOwnerOrAssignee(currentUser, task)) {
		    if (!permissionService.validateIfUserIsInitiatorAndCanCompleteTask(currentUser, task)) {
		        throw new NotPermittedException();
		    }
		}
  
        try {
            taskService.complete(task.getId());
        } catch (ActivitiException e) {
            log.error("Error completing task " + taskId, e);
            throw new BadRequestException("Task " + taskId + " can't be completed", e);
        }
    }
	
    public TaskRepresentation assignTask(String taskId, ObjectNode requestNode) {
		User currentUser = SecurityUtils.getCurrentUserObject();
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		
		if (task == null) {
            throw new NotFoundException("Task with id: " + taskId + " does not exist");
        }
		
		checkTaskPermissions(taskId, currentUser, task);

		if (requestNode.get("assignee") != null) {

        	 // This method can only be called by someone in a tenant. Check if the user is part of the tenant
            String assigneeIdString = requestNode.get("assignee").asText();
            Long assigneeId = null;
            try {
            	assigneeId = Long.valueOf(assigneeIdString);
            } catch (Exception e) {
            	throw new BadRequestException("Invalid assignee id");
            }
            
            CachedUser cachedUser = userCache.getUser(assigneeId);
            if (cachedUser == null) {
            	throw new BadRequestException("Invalid assignee id");
            }
            assignTask(currentUser, task, assigneeIdString);
        	
        } else if (requestNode.get("email") != null) {
        	
        	String email = validateEmail(requestNode);
    		
    		// There are two cases: there is a user with the given email or not.
    		// If there is a user already: we simply assign it to that user
    		// If there is no user in the system: set the task assignee to the email address (it will be linked during registration of that user)
    		
    		User user = userService.findOrCreateUserByEmail(email);
    		assignTask(currentUser, task, String.valueOf(user.getId()));

		} else {
        	throw new BadRequestException("Assignee or email is required");
        }
        
        task = taskService.createTaskQuery().taskId(taskId).singleResult();
        TaskRepresentation rep = new TaskRepresentation(task);
        TaskUtil.fillPermissionInformation(rep, task, currentUser, historyService, repositoryService);
        
        populateAssignee(task, rep);
        rep.setInvolvedPeople(getInvolvedUsers(taskId));
        return rep;
    }

	public void involveUser(String taskId, ObjectNode requestNode) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

		if (task == null) {
			throw new NotFoundException("Task with id: " + taskId + " does not exist");
		}

		User currentUser = SecurityUtils.getCurrentUserObject();
		permissionService.validateReadPermissionOnTask(currentUser, task.getId());

		if (requestNode.get("userId") != null) {
			Long userId = requestNode.get("userId").asLong();
			CachedUser user = userCache.getUser(userId);
			if (user == null) {
				throw new BadRequestException("Invalid user id");
			}
			taskService.addUserIdentityLink(taskId, userId.toString(), IdentityLinkType.PARTICIPANT);
			
		} else if (requestNode.get("email") != null) {
			
			String email = requestNode.get("email").asText();
			
			User user = userService.findOrCreateUserByEmail(email);
			taskService.addUserIdentityLink(taskId, String.valueOf(user.getId()), IdentityLinkType.PARTICIPANT);

		} else {
			throw new BadRequestException("User id or email is required");
		}

	}
	
	public void removeInvolvedUser(String taskId, ObjectNode requestNode) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

		if (task == null) {
			throw new NotFoundException("Task with id: " + taskId + " does not exist");
		}

		permissionService.validateReadPermissionOnTask(SecurityUtils.getCurrentUserObject(), task.getId());
		
		String assigneeString = null;
		if (requestNode.get("userId") != null) {
			Long userId = requestNode.get("userId").asLong();
			if (userCache.getUser(userId) == null) {
				throw new BadRequestException("Invalid user id");
			}
			assigneeString = String.valueOf(userId);
			
		} else if (requestNode.get("email") != null) {
			
			String email = requestNode.get("email").asText();
			assigneeString = email;
			
		} else {
			throw new BadRequestException("User id or email is required");
		}
		
		taskService.deleteUserIdentityLink(taskId, assigneeString, IdentityLinkType.PARTICIPANT);
	}
	
    public void claimTask(String taskId) {
		
		User currentUser = SecurityUtils.getCurrentUserObject();
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		
		if (task == null) {
            throw new NotFoundException("Task with id: " + taskId + " does not exist");
        }
		
		permissionService.validateReadPermissionOnTask(currentUser, task.getId());
        
        try {
            taskService.claim(task.getId(), String.valueOf(currentUser.getId()));
        } catch (ActivitiException e) {
            throw new BadRequestException("Task " + taskId + " can't be claimed", e);
        }
    }

    protected void checkTaskPermissions(String taskId, User currentUser, Task task) {
		permissionService.validateReadPermissionOnTask(currentUser, task.getId());
    }
	
	protected String validateEmail(ObjectNode requestNode) {
	    String email = requestNode.get("email") != null ? requestNode.get("email").asText() : null;
		if (email == null) {
			throw new BadRequestException("Email is mandatory");
		}
		return email;
    }

    protected void assignTask(User currentUser, Task task, String assigneeIdString) {
        try {
            String oldAssignee = task.getAssignee();
            taskService.setAssignee(task.getId(), assigneeIdString);

            // If the old assignee user wasn't part of the involved users yet, make it so
            addIdentiyLinkForUser(task, oldAssignee, IdentityLinkType.PARTICIPANT);

            // If the current user wasn't part of the involved users yet, make it so
            String currentUserIdString = String.valueOf(currentUser.getId());
            addIdentiyLinkForUser(task, currentUserIdString, IdentityLinkType.PARTICIPANT);

        } catch (ActivitiException e) {
            throw new BadRequestException("Task " + task.getId() + " can't be assigned", e);
        }
    }

    protected void addIdentiyLinkForUser(Task task, String userId, String linkType) {
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
        boolean isOldUserInvolved = false;
        for (IdentityLink identityLink : identityLinks) {
            if (userId.equals(identityLink.getUserId())
                    && (identityLink.getType().equals(IdentityLinkType.PARTICIPANT) || identityLink.getType().equals(IdentityLinkType.CANDIDATE))) {
                isOldUserInvolved = true;
            }
        }
        if (!isOldUserInvolved) {
            taskService.addUserIdentityLink(task.getId(), userId, linkType);
        }
    }

}
