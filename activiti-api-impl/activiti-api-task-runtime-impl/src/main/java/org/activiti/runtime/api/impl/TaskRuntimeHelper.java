package org.activiti.runtime.api.impl;

import java.util.List;
import java.util.Objects;

import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.payloads.UpdateTaskPayload;
import org.activiti.engine.TaskService;
import org.activiti.runtime.api.model.impl.APITaskConverter;

public class TaskRuntimeHelper {
    private final TaskService taskService;
    private final SecurityManager securityManager;
    private final UserGroupManager userGroupManager;
    private final APITaskConverter taskConverter;
    
    public TaskRuntimeHelper(TaskService taskService,
                       APITaskConverter taskConverter,
                       SecurityManager securityManager,
                       UserGroupManager userGroupManager) {
        this.taskService = taskService;
        this.securityManager=securityManager;
        this.userGroupManager = userGroupManager;
        this.taskConverter = taskConverter;
    }
    
    public Task applyUpdateTaskPayload(boolean isAdmin, UpdateTaskPayload updateTaskPayload) {
        
        org.activiti.engine.task.Task internalTask=null;
        int updates=0;
        String oldValue,newValue;
        
        if (isAdmin) {
            internalTask = taskService.createTaskQuery().taskId(updateTaskPayload.getTaskId()).singleResult();
        } else {
            internalTask=getInternalTaskWithChecks(updateTaskPayload.getTaskId());
        }
        
        if (internalTask == null) {
            throw new NotFoundException("Unable to find task for the given id: " + updateTaskPayload.getTaskId());
        }
          
        if ((newValue = updateTaskPayload.getName()) != null) {
            oldValue = internalTask.getName();
            if (!Objects.equals(oldValue,newValue)) {
                updates++;
                internalTask.setName(newValue);
            }
        }
        
        if ((newValue = updateTaskPayload.getDescription()) != null) {
            oldValue = internalTask.getDescription();
            if (!Objects.equals(oldValue,newValue)) {
                updates++;
                internalTask.setDescription(newValue);
            }
        }
            
        if (updateTaskPayload.getPriority() != null) {
            if (internalTask.getPriority()!=updateTaskPayload.getPriority()) {
                updates++;
                internalTask.setPriority(updateTaskPayload.getPriority());
            }
        }
        
        if (updateTaskPayload.getDueDate() != null) {
            if (!Objects.equals(internalTask.getDueDate(),updateTaskPayload.getDueDate())) {
                updates++;
                internalTask.setDueDate(updateTaskPayload.getDueDate());
            }
        }
        
        //@TODO: check if this value can be updated
        if ((newValue=updateTaskPayload.getParentTaskId()) != null) {
            oldValue = internalTask.getParentTaskId();
            if (!Objects.equals(oldValue,newValue)) {
                updates++;
                internalTask.setParentTaskId(newValue);
            }
        }
        
        if ((newValue=updateTaskPayload.getFormKey()) != null) {
            oldValue = internalTask.getFormKey();
            if (!Objects.equals(oldValue,newValue)) {
                updates++;
                internalTask.setFormKey(newValue);
            }
        }
        
        if (updates > 0) {
            taskService.saveTask(internalTask);
        }
        
        return taskConverter.from(getInternalTask(updateTaskPayload.getTaskId()));                   
    }
    
    public org.activiti.engine.task.Task getInternalTaskWithChecks(String taskId) {
        String authenticatedUserId = securityManager!=null ? securityManager.getAuthenticatedUserId() : null;
        
        if (authenticatedUserId != null && !authenticatedUserId.isEmpty() && userGroupManager!=null) {
            
            List<String> userRoles = userGroupManager.getUserRoles(authenticatedUserId);
            List<String> userGroups = userGroupManager.getUserGroups(authenticatedUserId);
            org.activiti.engine.task.Task task = taskService.createTaskQuery().taskCandidateOrAssigned(authenticatedUserId,
                                                                                                       userGroups).taskId(taskId).singleResult();
            if (task == null) {
                throw new NotFoundException("Unable to find task for the given id: " + taskId + " for user: " + authenticatedUserId + " (with groups: " + userGroups + " & with roles: " + userRoles + ")");
            }
            
            // validate that you are trying to update task where you are the assignee
            if (!Objects.equals(task.getAssignee(),authenticatedUserId)) {
                throw new IllegalStateException("You cannot update a task where you are not the assignee");
            }
            return task;
        }
        throw new IllegalStateException("There is no authenticated user, we need a user authenticated to find tasks");
    }
    
    public org.activiti.engine.task.Task getInternalTask(String taskId) {
        org.activiti.engine.task.Task internalTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (internalTask == null) {
            throw new NotFoundException("Unable to find task for the given id: " + taskId);
        }
        return internalTask;
    }

}
