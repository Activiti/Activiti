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
import java.util.Map;

import org.activiti.engine.Page;
import org.activiti.engine.Task;
import org.activiti.engine.impl.persistence.task.TaskEntity;
import org.activiti.engine.impl.persistence.task.TaskInvolvement;


/**
 * @author Tom Baeyens
 */
public interface TaskSession {

  /* Task */
  TaskEntity findTask(String taskId);
  List<TaskEntity> findTasksByExecution(String executionId);
  List<Task> findTasksByAssignee(String assignee);
  List<Task> findCandidateTasks(String userId, List<String> groupIds);
  
  List<Task> dynamicFindTasks(Map<String, Object> params, Page page);
  long dynamicFindTaskCount(Map<String, Object> params);

  /* TaskInvolvement */
  List<TaskInvolvement> findTaskInvolvementsByTask(String taskId);
  

}
