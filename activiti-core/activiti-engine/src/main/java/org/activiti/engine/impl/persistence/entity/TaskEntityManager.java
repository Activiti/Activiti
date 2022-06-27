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

package org.activiti.engine.impl.persistence.entity;

import java.util.List;
import java.util.Map;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.task.Task;
@Internal
public interface TaskEntityManager extends EntityManager<TaskEntity> {

  void insert(TaskEntity taskEntity, ExecutionEntity execution);

  void changeTaskAssignee(TaskEntity taskEntity, String assignee);

  void changeTaskAssigneeNoEvents(TaskEntity taskEntity, String assignee);

  void changeTaskOwner(TaskEntity taskEntity, String owner);

  List<TaskEntity> findTasksByExecutionId(String executionId);

  List<TaskEntity> findTasksByProcessInstanceId(String processInstanceId);

  List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery);

  List<Task> findTasksAndVariablesByQueryCriteria(TaskQueryImpl taskQuery);

  long findTaskCountByQueryCriteria(TaskQueryImpl taskQuery);

  List<Task> findTasksByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults);

  long findTaskCountByNativeQuery(Map<String, Object> parameterMap);

  List<Task> findTasksByParentTaskId(String parentTaskId);

  void updateTaskTenantIdForDeployment(String deploymentId, String newTenantId);

  void deleteTask(String taskId, String deleteReason, boolean cascade);

  void deleteTask(String taskId, String deleteReason, boolean cascade, boolean cancel);

  void deleteTasksByProcessInstanceId(String processInstanceId, String deleteReason, boolean cascade);

  void deleteTask(TaskEntity task, String deleteReason, boolean cascade, boolean cancel);

}
