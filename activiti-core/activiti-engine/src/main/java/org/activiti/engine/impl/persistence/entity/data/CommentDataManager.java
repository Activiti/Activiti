/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.persistence.entity.data;

import java.util.List;

import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;


public interface CommentDataManager extends DataManager<CommentEntity> {

  List<Comment> findCommentsByTaskId(String taskId);

  List<Comment> findCommentsByTaskIdAndType(String taskId, String type);

  List<Comment> findCommentsByType(String type);

  List<Event> findEventsByTaskId(String taskId);

  List<Event> findEventsByProcessInstanceId(String processInstanceId);

  void deleteCommentsByTaskId(String taskId);

  void deleteCommentsByProcessInstanceId(String processInstanceId);

  List<Comment> findCommentsByProcessInstanceId(String processInstanceId);

  List<Comment> findCommentsByProcessInstanceId(String processInstanceId, String type);

  Comment findComment(String commentId);

  Event findEvent(String commentId);

}
