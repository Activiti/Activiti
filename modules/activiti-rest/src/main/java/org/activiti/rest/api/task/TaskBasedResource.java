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

package org.activiti.rest.api.task;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.SecuredResource;


/**
 * Shared logic for resources related to Tasks.
 * 
 * @author Frederik Heremans
 */
public class TaskBasedResource extends SecuredResource {

  protected DelegationState getDelegationState(String delegationState) {
    DelegationState state = null;
    if(delegationState != null) {
      if(DelegationState.RESOLVED.name().toLowerCase().equals(delegationState)) {
        return DelegationState.RESOLVED;
      } else if(DelegationState.PENDING.name().toLowerCase().equals(delegationState)) {
        return DelegationState.PENDING;
      } else {
        throw new ActivitiIllegalArgumentException("Illegal value for delegationState: " + delegationState);
      }
    }
    return state;
  }
  
  /**
   * Populate the task based on the values that are present in the given {@link TaskRequest}.
   */
  protected void populateTaskFromRequest(Task task, TaskRequest taskRequest) {
    if(taskRequest.isNameSet()) {
      task.setName(taskRequest.getName());
    }
    if(taskRequest.isAssigneeSet()) {
      task.setAssignee(taskRequest.getAssignee());
    }
    if(taskRequest.isDescriptionSet()) {
      task.setDescription(taskRequest.getDescription());
    }
    if(taskRequest.isDuedateSet()) {
      task.setDueDate(taskRequest.getDueDate());
    }
    if(taskRequest.isOwnerSet()) {
      task.setOwner(taskRequest.getOwner());
    }
    if(taskRequest.isParentTaskIdSet()) {
      task.setParentTaskId(taskRequest.getParentTaskId());
    }
    if(taskRequest.isPrioritySet()) {
      task.setPriority(taskRequest.getPriority());
    }

    if(taskRequest.isDelegationStateSet()) {
      DelegationState delegationState = getDelegationState(taskRequest.getDelegationState());
      task.setDelegationState(delegationState);
    }
  }
}
