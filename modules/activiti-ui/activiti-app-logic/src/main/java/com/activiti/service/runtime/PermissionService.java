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
package com.activiti.service.runtime;

import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.RelatedContent;
import com.activiti.domain.runtime.RuntimeAppDeployment;
import com.activiti.repository.runtime.RuntimeAppDeploymentRepository;
import com.activiti.repository.runtime.RuntimeAppRepository;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.exception.NotPermittedException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized service for all permission-checks.
 * 
 * @author Frederik Heremans
 */
@Service
public class PermissionService {
	
	@Autowired
	protected TaskService taskService;
	
	@Autowired
	protected RuntimeService runtimeService;
	
	@Autowired
	protected RepositoryService repositoryService;
	
	@Autowired 
	protected HistoryService historyService;

	@Autowired
	protected RuntimeAppRepository runtimeAppRepository;
	
	@Autowired
	protected RuntimeAppDeploymentRepository runtimeAppDeploymentRepository;
	
	/**
	 * Check if the given user is allowed to read the task.
	 */
	public HistoricTaskInstance validateReadPermissionOnTask(User user, String taskId) {
		
		List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
				.taskId(taskId)
				.taskInvolvedUser(String.valueOf(user.getId()))
				.list();
		
		if (CollectionUtils.isNotEmpty(tasks)) {
			return tasks.get(0);
		}
		
		// Task is maybe accessible through groups of user
		HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery();
		historicTaskInstanceQuery.taskId(taskId);
		
		List<String> groupIds = getGroupIdsForUser(user);
		if (!groupIds.isEmpty()) {
			historicTaskInstanceQuery.taskCandidateGroupIn(getGroupIdsForUser(user));
		}
		
		tasks = historicTaskInstanceQuery.list();
		if (CollectionUtils.isNotEmpty(tasks)) {
			return tasks.get(0);
		}
		
		// Last resort: user has access to proc inst -> can see task
		tasks = historyService.createHistoricTaskInstanceQuery().taskId(taskId).list();
		if (CollectionUtils.isNotEmpty(tasks)) {
		    HistoricTaskInstance task = tasks.get(0);
    		if (task != null && task.getProcessInstanceId() != null) {
    			boolean hasReadPermissionOnProcessInstance = hasReadPermissionOnProcessInstance(user, task.getProcessInstanceId());
    			if (hasReadPermissionOnProcessInstance) {
    			    return task;
    			}
    		}
		}
		throw new NotPermittedException("User is not allowed to work with task " + taskId);
	}

	private List<String> getGroupIdsForUser(User user) {
	    List<String> groupIds = new ArrayList<String>(user.getGroups().size());
		for (Group group : user.getGroups()) {
			groupIds.add(String.valueOf(group.getId()));
		}
	    return groupIds;
    }
    
    public boolean isTaskOwnerOrAssignee(User user, String taskId) {
    	return isTaskOwnerOrAssignee(user, taskService.createTaskQuery().taskId(taskId).singleResult());
    }
    
    public boolean isTaskOwnerOrAssignee(User user, Task task) {
    	String currentUser = String.valueOf(user.getId());
    	return currentUser.equals(task.getAssignee()) || currentUser.equals(task.getOwner());
    }
    
    public boolean validateIfUserIsInitiatorAndCanCompleteTask(User user, Task task) {
        boolean canCompleteTask = false;
        if (task.getProcessInstanceId() != null) {
            HistoricProcessInstance historicProcessInstance = 
                    historyService.createHistoricProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            if (historicProcessInstance != null && StringUtils.isNotEmpty(historicProcessInstance.getStartUserId())) {
                String processInstanceStartUserId = historicProcessInstance.getStartUserId();
                if (String.valueOf(user.getId()).equals(processInstanceStartUserId)) {
                    BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                    FlowElement flowElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                    if (flowElement != null && flowElement instanceof UserTask) {
                        UserTask userTask = (UserTask) flowElement;
                        List<ExtensionElement> extensionElements = userTask.getExtensionElements().get("initiator-can-complete");
                        if (CollectionUtils.isNotEmpty(extensionElements)) {
                            String value = extensionElements.get(0).getElementText();
                            if (StringUtils.isNotEmpty(value) && Boolean.valueOf(value)) {
                                canCompleteTask = true;
                            }
                        }
                    }
                }
            }
        }
        return canCompleteTask;
    }
    
    public boolean isInvolved(User user, String taskId) {
    	return historyService.createHistoricTaskInstanceQuery().taskId(taskId).taskInvolvedUser(String.valueOf(user.getId())).count() == 1;
    }
    
    /**
     * Check if the given user is allowed to read the process instance.
     */
    public boolean hasReadPermissionOnProcessInstance(User user, String processInstanceId) {
        HistoricProcessInstance historicProcessInstance = 
                historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        return hasReadPermissionOnProcessInstance(user, historicProcessInstance, processInstanceId);
    }
    
	/**
	 * Check if the given user is allowed to read the process instance.
	 */
	public boolean hasReadPermissionOnProcessInstance(User user,  HistoricProcessInstance historicProcessInstance, String processInstanceId) {
		if (historicProcessInstance == null) {
		    throw new NotFoundException("Process instance not found for id " + processInstanceId);
		}

		// Start user check
		if (historicProcessInstance.getStartUserId() != null 
				&& Long.valueOf(historicProcessInstance.getStartUserId()).equals(user.getId())) {
			return true;
		}
		
		// check if the user is involved in the task
		HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery();
		historicProcessInstanceQuery.processInstanceId(processInstanceId);
		historicProcessInstanceQuery.involvedUser(String.valueOf(user.getId()));
		if (historicProcessInstanceQuery.count() > 0) {
			return true;
		}

		// Visibility: check if there are any tasks for the current user
		HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery();
		historicTaskInstanceQuery.processInstanceId(processInstanceId);
		historicTaskInstanceQuery.taskInvolvedUser(String.valueOf(user.getId()));
        if (historicTaskInstanceQuery.count() > 0) {
            return true;
        }

        List<String> groupIds = getGroupIdsForUser(user);
        if (!groupIds.isEmpty()) {
            historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery();
            historicTaskInstanceQuery.processInstanceId(processInstanceId)
                .taskCandidateGroupIn(groupIds);
            return historicTaskInstanceQuery.count() > 0;
        }
        
        return false;	
	}
	
    public boolean canAddRelatedContentToTask(User user, String taskId) {
        validateReadPermissionOnTask(user, taskId);
        return true;
    }
    
    public boolean canAddRelatedContentToProcessInstance(User user, String processInstanceId) {
    	return hasReadPermissionOnProcessInstance(user, processInstanceId);
    }

    public boolean canDownloadContent(User currentUserObject, RelatedContent content) {
    	if (content.getTaskId() != null) {
    	    validateReadPermissionOnTask(currentUserObject, content.getTaskId());
    	    return true;
    	} else if (content.getProcessInstanceId() != null) {
    		return hasReadPermissionOnProcessInstance(currentUserObject, content.getProcessInstanceId());
    	} else {
    		return false;
    	}
    }

    public boolean hasWritePermissionOnRelatedContent(User user, RelatedContent content) {
    	if (content.getProcessInstanceId() != null) {
    		return hasReadPermissionOnProcessInstance(user, content.getProcessInstanceId());
    	} else {
    	    if(content.getCreatedBy() != null) {
    	        return new EqualsBuilder().append(user.getId(), content.getCreatedBy().getId()).isEquals();
    	    } else {
    	        return false;
    	    }
    	}
    }
    
    public boolean hasReadPermissionOnRuntimeApp(User user, Long appId) {
    	return runtimeAppRepository.findByUserAndAppDefinitionId(user, appId) != null;
    }
    
    public boolean hasReadPermissionOnProcessDefinition(User user, String processDefinitionId) {
    	
    	// Get deployment id for process definition
    	ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
    	String deploymentId = processDefinition.getDeploymentId();
    	
    	// Get runtime app for deployment 
    	RuntimeAppDeployment runtimeAppDeployment = runtimeAppDeploymentRepository.findByDeploymentId(deploymentId);
    	return hasReadPermissionOnRuntimeApp(user, runtimeAppDeployment.getAppDefinition().getId()); 
    }
    
    public ProcessDefinition getProcessDefinitionById(String processDefinitionId) {
        return repositoryService.getProcessDefinition(processDefinitionId);
    }

    public boolean canDeleteProcessInstance(User currentUser, HistoricProcessInstance processInstance) {
        boolean canDelete = false;
        if(processInstance.getStartUserId() != null) {
            try {
                Long starterId = Long.parseLong(processInstance.getStartUserId());
                canDelete = starterId.equals(currentUser.getId());
            } catch(NumberFormatException nfe) {
                // Ignore illegal starter id value
            }
        }

        return canDelete;
    }
	
}
