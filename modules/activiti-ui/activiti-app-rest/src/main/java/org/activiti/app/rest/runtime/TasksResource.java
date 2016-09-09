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
import javax.servlet.http.HttpServletRequest;

import org.activiti.app.model.runtime.CreateTaskRepresentation;
import org.activiti.app.model.runtime.TaskRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class TasksResource {

  @Inject
  protected TaskService taskService;

  @RequestMapping(value = "/rest/tasks", method = RequestMethod.POST)
  public TaskRepresentation createNewTask(@RequestBody CreateTaskRepresentation taskRepresentation, HttpServletRequest request) {
    if (StringUtils.isEmpty(taskRepresentation.getName())) {
      throw new BadRequestException("Task name is required");
    }

    Task task = taskService.newTask();
    task.setName(taskRepresentation.getName());
    task.setDescription(taskRepresentation.getDescription());
    if (StringUtils.isNotEmpty(taskRepresentation.getCategory())) {
      task.setCategory(taskRepresentation.getCategory());
    }
    task.setAssignee(SecurityUtils.getCurrentUserId());
    taskService.saveTask(task);
    return new TaskRepresentation(taskService.createTaskQuery().taskId(task.getId()).singleResult());
  }

}
