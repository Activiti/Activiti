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
package org.activiti.engine.impl.cmd;

import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

/**

 */
public class ClaimTaskCmd extends NeedsActiveTaskCmd<Void> {

  private static final long serialVersionUID = 1L;

  protected String userId;

  public ClaimTaskCmd(String taskId, String userId) {
    super(taskId);
    this.userId = userId;
  }

  protected Void execute(CommandContext commandContext, TaskEntity task) {
    if (userId != null) {
      task.setClaimTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());

      if (task.getAssignee() != null) {
        if (!task.getAssignee().equals(userId)) {
          // When the task is already claimed by another user, throw
          // exception. Otherwise, ignore
          // this, post-conditions of method already met.
          throw new ActivitiTaskAlreadyClaimedException(task.getId(), task.getAssignee());
        }
      } else {
        commandContext.getTaskEntityManager().changeTaskAssignee(task, userId);
      }
    } else {
      // Task claim time should be null
      task.setClaimTime(null);

      // Task should be assigned to no one
      commandContext.getTaskEntityManager().changeTaskAssignee(task, null);
    }

    // Add claim time to historic task instance
    commandContext.getHistoryManager().recordTaskClaim(task);

    return null;
  }

  @Override
  protected String getSuspendedTaskException() {
    return "Cannot claim a suspended task";
  }

}
