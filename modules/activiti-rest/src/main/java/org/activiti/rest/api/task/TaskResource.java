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
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.restlet.resource.Get;

/**
 * @author Frederik Heremans
 */
public class TaskResource extends SecuredResource {

  @Get
  public TaskResponse getTask() {
    String taskId = getAttribute("taskId");

    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("The taskId cannot be null");
    }

    Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    if (task == null) {
      throw new ActivitiObjectNotFoundException("Could not find a task with id '" + taskId + "'.", Task.class);
    }
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory().createTaskReponse(this, task);
  }

}
