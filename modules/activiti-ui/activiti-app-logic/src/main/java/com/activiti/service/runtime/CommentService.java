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

import java.util.List;

import javax.inject.Inject;

import org.activiti.engine.runtime.Clock;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.Comment;
import com.activiti.repository.runtime.CommentRepository;

/**
 * @author Frederik Heremans
 */
@Service
public class CommentService {

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private Clock clock;

    public Long countCommentsForTask(String taskId) {
        return commentRepository.countByTaskId(taskId);
    }

    public Long countCommentsForProcessInstance(String processInstanceId) {
        return commentRepository.countByProcessInstanceId(processInstanceId);
    }

    public List<Comment> getCommentsForTask(String taskId, boolean latestFirst) {
        return commentRepository.findByTaskId(taskId, new Sort((latestFirst ? Direction.DESC : Direction.ASC), Comment.PROPERTY_CREATED));
    }

    public List<Comment> getCommentsForProcessInstance(String processInstanceId, boolean latestFirst) {
        return commentRepository.findByProcessInstanceId(processInstanceId, new Sort((latestFirst ? Direction.DESC : Direction.ASC), Comment.PROPERTY_CREATED));
    }

    public Comment createComment(String message, User createdBy, String processInstanceId) {
        return createComment(message, createdBy, null, processInstanceId);
    }

    public Comment createComment(String message, User createdBy, String taskId, String processInstanceId) {
        Comment newComment = new Comment();
        newComment.setMessage(message);
        newComment.setCreatedBy(createdBy);
        newComment.setCreated(clock.getCurrentTime());
        newComment.setTaskId(taskId);
        newComment.setProcessInstanceId(processInstanceId);

        commentRepository.save(newComment);
        return newComment;
    }
    
    public void deleteComment(Comment comment) {
        commentRepository.delete(comment);
    }
    
    /**
     * Deletes all comments related to the given process instance. Includes both comments on the process instance itself
     * and any comments on the tasks in that process.
     */
    @Transactional
    public void deleteAllCommentsForProcessInstance(String processInstanceId) {
        commentRepository.deleteAllByProcessInstanceId(processInstanceId);
    }

}
