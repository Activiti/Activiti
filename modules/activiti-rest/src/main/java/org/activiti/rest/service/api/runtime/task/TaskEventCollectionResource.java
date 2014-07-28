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

package org.activiti.rest.service.api.runtime.task;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Event;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.EventResponse;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.resource.Get;


/**
 * @author Frederik Heremans
 */
public class TaskEventCollectionResource extends TaskBaseResource {

  @Get
  public List<EventResponse> getEvents() {
    if(!authenticate())
      return null;
    
    List<EventResponse> result = new ArrayList<EventResponse>();
    RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
    HistoricTaskInstance task = getHistoricTaskFromRequest();
    
    for(Event event : ActivitiUtil.getTaskService().getTaskEvents(task.getId())) {
      result.add(responseFactory.createEventResponse(this, event));
    }
    
    return result;
  }
}
