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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.apache.commons.lang3.StringUtils;

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.Comment;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.runtime.CommentRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.exception.NotPermittedException;
import com.activiti.service.runtime.CommentService;
import com.activiti.service.runtime.PermissionService;

public class AbstractCommentsResource {

    @Inject
    private PermissionService permissionService;

    @Inject
    private CommentService commentService;
    
    @Inject
    private HistoryService historyService;

    public ResultListDataRepresentation getTaskComments(String taskId, Boolean latestFirst) {

        User currentUser = SecurityUtils.getCurrentUserObject();
        checkReadPermissionOnTask(currentUser, taskId);
        List<Comment> comments = commentService.getCommentsForTask(taskId, Boolean.TRUE.equals(latestFirst));

        // Create representation for all comments
        List<CommentRepresentation> commentList = new ArrayList<CommentRepresentation>();
        for (Comment comment : comments) {
            commentList.add(new CommentRepresentation(comment));
        }

        return new ResultListDataRepresentation(commentList);
    }

    public CommentRepresentation addTaskComment(CommentRepresentation commentRequest, String taskId) {

        if (StringUtils.isBlank(commentRequest.getMessage())) {
            throw new BadRequestException("Comment should not be empty");
        }
        
        HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new NotFoundException("No task found with id: " + taskId);
        }

        // Check read permission and message
        User currentUser = SecurityUtils.getCurrentUserObject();
        checkReadPermissionOnTask(currentUser, taskId);

        // Create comment
        Comment comment = commentService.createComment(commentRequest.getMessage(), currentUser, task.getId(), task.getProcessInstanceId());
        return new CommentRepresentation(comment);
    }
    
    public ResultListDataRepresentation getProcessInstanceComments(String processInstanceId, Boolean latestFirst) {

        User currentUser = SecurityUtils.getCurrentUserObject();
        checkReadPermissionOnProcessInstance(currentUser, processInstanceId);
        List<Comment> comments = commentService.getCommentsForProcessInstance(processInstanceId, Boolean.TRUE.equals(latestFirst));

        // Create representation for all comments
        List<CommentRepresentation> commentList = new ArrayList<CommentRepresentation>();
        for (Comment comment : comments) {
            commentList.add(new CommentRepresentation(comment));
        }

        return new ResultListDataRepresentation(commentList);
    }
    
    public CommentRepresentation addProcessInstanceComment(CommentRepresentation commentRequest, String processInstanceId) {

        if (StringUtils.isBlank(commentRequest.getMessage())) {
            throw new BadRequestException("Comment should not be empty");
        }
        
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (processInstance == null) {
            throw new NotFoundException("No process instance found with id: " + processInstanceId);
        }

        // Check read permission and message
        User currentUser = SecurityUtils.getCurrentUserObject();
        checkReadPermissionOnProcessInstance(currentUser, processInstanceId);

        // Create comment
        Comment comment = commentService.createComment(commentRequest.getMessage(), currentUser, processInstanceId);
        return new CommentRepresentation(comment);
    }

    protected void checkReadPermissionOnTask(User user, String taskId) {
        if (taskId == null) {
            throw new BadRequestException("Task id is required");
        }
        permissionService.validateReadPermissionOnTask(SecurityUtils.getCurrentUserObject(), taskId);
    }
    
    protected void checkReadPermissionOnProcessInstance(User user, String processInstanceId) {
        if (processInstanceId == null) {
            throw new BadRequestException("Process instance id is required");
        }
        if (!permissionService.hasReadPermissionOnProcessInstance(SecurityUtils.getCurrentUserObject(), processInstanceId)) {
            throw new NotPermittedException("You are not permitted to read process instance with id: " + processInstanceId);
        }
    }
}
