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
package org.activiti.explorer.ui.task.data;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.TaskQuery;


/**
 * @author Joram Barrez
 */
public class QueuedListQuery extends AbstractTaskListQuery {
  
  protected String groupId;
  protected TaskService taskService;
  
  public QueuedListQuery(String groupId) {
    this.groupId = groupId;
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
  }
  
  @Override
  protected TaskQuery getQuery() {
    return taskService.createTaskQuery().taskCandidateGroup(groupId).taskUnnassigned().orderByTaskId().asc();
  }

}
