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

package org.activiti.engine.impl.cfg;

import java.util.List;

import org.activiti.engine.Page;
import org.activiti.engine.Task;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.persistence.task.TaskEntity;
import org.activiti.engine.impl.persistence.task.TaskInvolvementEntity;


/**
 * @author Tom Baeyens
 */
public interface TaskSession {

  void insertTask(TaskEntity taskEntity);
  void deleteTask(String taskId);

  TaskEntity findTaskById(String taskId);
  List<TaskEntity> findTasksByExecutionId(String executionId);
  List<Task> findTasksByAssignee(String assignee);
  List<Task> findCandidateTasks(String userId, List<String> groupIds);
  
  List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery, Page page);
  long findTaskCountByQueryCriteria(TaskQueryImpl taskQuery);

  /* TaskInvolvementEntity */
  void deleteTaskInvolvement(TaskInvolvementEntity taskInvolvement);
  void insertTaskInvolvement(TaskInvolvementEntity taskInvolvement);

  List<TaskInvolvementEntity> findTaskInvolvementsByTaskId(String taskId);
}
