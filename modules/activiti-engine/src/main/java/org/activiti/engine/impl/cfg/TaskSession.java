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

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.task.IdentityLinkEntity;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.task.Task;


/**
 * @author Tom Baeyens
 */
public interface TaskSession {

  TaskEntity findTaskById(String taskId);
  
  List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery, Page page);
  long findTaskCountByQueryCriteria(TaskQueryImpl taskQuery);

  List<IdentityLinkEntity> findIdentityLinksByTaskId(String taskId);
  List<IdentityLinkEntity> findIdentityLinkByTaskUserGroupAndType(String taskId, String userId, String groupId, String type);

  HistoricTaskInstance findHistoricTaskInstanceById(String taskId);
  long findHistoricTaskInstanceCountByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQueryImpl);
  List<HistoricTaskInstance> findHistoricTaskInstancesByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQueryImpl, Page page);
  void deleteHistoricTaskInstance(String taskId);

}
