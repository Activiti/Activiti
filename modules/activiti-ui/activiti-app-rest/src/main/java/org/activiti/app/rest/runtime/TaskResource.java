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
package org.activiti.app.rest.runtime;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.activiti.app.model.runtime.TaskRepresentation;
import org.activiti.app.model.runtime.TaskUpdateRepresentation;
import org.activiti.app.service.runtime.PermissionService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class TaskResource extends AbstractTaskResource {

    private final Logger log = LoggerFactory.getLogger(TaskResource.class);
    
    @Inject
    protected TaskService taskService;
    
    @Inject
    protected PermissionService permissionService;
    
    @Inject
    protected RepositoryService repositoryService;
    
    @RequestMapping(value = "/rest/tasks/{taskId}",
            method = RequestMethod.GET,
            produces = "application/json")
    public TaskRepresentation getTask(@PathVariable String taskId, HttpServletResponse response) {
    	return super.getTask(taskId, response);
    }

    @RequestMapping(value = "/rest/tasks/{taskId}",
            method = RequestMethod.PUT,
            produces = "application/json")
    public TaskRepresentation updateTask(@PathVariable("taskId") String taskId, @RequestBody TaskUpdateRepresentation updated) {
        return super.updateTask(taskId, updated);
    }
    
}
