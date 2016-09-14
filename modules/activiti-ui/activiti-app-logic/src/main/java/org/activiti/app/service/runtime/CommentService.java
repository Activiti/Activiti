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
package org.activiti.app.service.runtime;

import java.util.List;

import javax.inject.Inject;

import org.activiti.app.domain.runtime.Comment;
import org.activiti.app.repository.runtime.CommentRepository;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.Clock;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        newComment.setCreatedBy(createdBy.getId());
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
