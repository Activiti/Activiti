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
package com.activiti.rest.runtime;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.TaskService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.runtime.TaskRepresentation;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class TasksResource extends AbstractTasksResource {

    @Inject
    protected TaskService taskService;
    
	@RequestMapping(value = "/rest/tasks", method = RequestMethod.POST)
    public TaskRepresentation createNewTask(@RequestBody TaskRepresentation taskRepresentation, HttpServletRequest request) {
		return super.createNewTask(taskRepresentation, request);
    }
}
