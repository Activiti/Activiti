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

import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class TaskResource extends SecuredResource {
  
  @Get
  public TaskResponse getTasks() {
    if(authenticate() == false) return null;
    String taskId = (String) getRequest().getAttributes().get("taskId");
    Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    TaskResponse response = new TaskResponse(task);
    
    TaskFormData taskFormData = ActivitiUtil.getFormService().getTaskFormData(taskId);
    if(taskFormData != null) {
      response.setFormResourceKey(taskFormData.getFormKey());     
    }
    
    return response;
  }

}
