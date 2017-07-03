/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 *
 */

package org.activiti.services;

import java.util.Map;

import org.activiti.client.model.Task;
import org.activiti.client.model.resources.TaskResource;
import org.activiti.client.model.resources.assembler.TaskResourceAssembler;
import org.activiti.engine.TaskService;
import org.activiti.model.converter.TaskConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**

 */
@RestController
@RequestMapping(value = "/api/runtime/tasks", produces = "application/hal+json")
public class TaskController {

    private final TaskService taskService;
    private final TaskConverter taskConverter;
    private final TaskResourceAssembler taskResourceAssembler;
    private final PageableTaskService pageableTaskService;

    @Autowired
    public TaskController(TaskService taskService,
                          TaskConverter taskConverter,
                          TaskResourceAssembler taskResourceAssembler,
                          PageableTaskService pageableTaskService) {
        this.taskService = taskService;
        this.taskConverter = taskConverter;
        this.taskResourceAssembler = taskResourceAssembler;
        this.pageableTaskService = pageableTaskService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<TaskResource> getTasks(Pageable pageable, PagedResourcesAssembler<Task> pagedResourcesAssembler) {
        Page<Task> page = pageableTaskService.getTasks(pageable);
        return pagedResourcesAssembler.toResource(page, taskResourceAssembler);
    }

    @RequestMapping(value = "/{taskId}", method = RequestMethod.GET)
    public Resource<Task> getTask(@PathVariable String taskId) {
        org.activiti.engine.task.Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        return taskResourceAssembler.toResource(taskConverter.from(task));
    }

    @RequestMapping(value = "/{taskId}/claim", method = RequestMethod.POST)
    public Resource<Task> claimTask(@PathVariable String taskId, @RequestParam("assignee") String assignee) {
        taskService.claim(taskId, assignee);
        Task task = taskConverter.from(taskService.createTaskQuery().taskId(taskId).singleResult());
        return  taskResourceAssembler.toResource(task);
    }

    @RequestMapping(value = "/{taskId}/complete", method = RequestMethod.POST)
    public ResponseEntity<Void> completeTask(@PathVariable String taskId, @RequestBody(required = false) Map<String, Object> variables) {
        taskService.complete(taskId, variables);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
