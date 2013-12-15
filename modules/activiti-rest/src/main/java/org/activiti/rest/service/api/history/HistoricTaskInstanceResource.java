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

package org.activiti.rest.service.api.history;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;


/**
 * @author Tijs Rademakers
 */
public class HistoricTaskInstanceResource extends SecuredResource {

  @Get
  public HistoricTaskInstanceResponse getTaskInstance() {
    if(!authenticate()) {
      return null;
    }
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createHistoricTaskInstanceResponse(this, getHistoricTaskInstanceFromRequest());
  }
  
  @Delete
  public void deleteTaskInstance() {
    if(!authenticate()) {
      return;
    }
    
    String taskId = getAttribute("taskId");
    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("The taskId cannot be null");
    }
    
    ActivitiUtil.getHistoryService().deleteHistoricTaskInstance(taskId);
  }
  
  protected HistoricTaskInstance getHistoricTaskInstanceFromRequest() {
    String taskId = getAttribute("taskId");
    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("The taskId cannot be null");
    }
    
    HistoricTaskInstance taskInstance = ActivitiUtil.getHistoryService().createHistoricTaskInstanceQuery()
           .taskId(taskId).singleResult();
    if (taskInstance == null) {
      throw new ActivitiObjectNotFoundException("Could not find a task instance with id '" + taskId + "'.", HistoricTaskInstance.class);
    }
    return taskInstance;
  }
}
