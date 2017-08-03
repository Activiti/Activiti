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

package org.activiti.services.rest.controllers;

import java.util.Map;

import org.activiti.engine.TaskService;
import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.Task;
import org.activiti.services.core.model.commands.CompleteTaskCmd;
import org.activiti.services.core.model.converter.TaskConverter;
import org.activiti.services.core.pageable.AuthenticationWrapper;
import org.activiti.services.core.pageable.PageableTaskService;
import org.activiti.services.rest.resources.TaskResource;
import org.activiti.services.rest.resources.assembler.TaskResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/tasks", produces = MediaTypes.HAL_JSON_VALUE)
public class TaskController {


    private ProcessEngineWrapper processEngine;

    private final TaskResourceAssembler taskResourceAssembler;

    private AuthenticationWrapper authenticationWrapper = new AuthenticationWrapper();

    @Autowired
    public TaskController(ProcessEngineWrapper processEngine,
                          TaskResourceAssembler taskResourceAssembler) {
        this.processEngine = processEngine;
        this.taskResourceAssembler = taskResourceAssembler;

    }

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<TaskResource> getTasks(Pageable pageable,
                                                 PagedResourcesAssembler<Task> pagedResourcesAssembler) {
        Page<Task> page = pageableTaskService.getTasks(pageable);
        return pagedResourcesAssembler.toResource(page,
                                                  taskResourceAssembler);
    }

    @RequestMapping(value = "/{taskId}", method = RequestMethod.GET)
    public Resource<Task> getTaskById(@PathVariable String taskId) {
        org.activiti.engine.task.Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        return taskResourceAssembler.toResource(taskConverter.from(task));
    }

    @RequestMapping(value = "/{taskId}/claim", method = RequestMethod.POST)
    public Resource<Task> claimTask(@PathVariable String taskId) {
        String assignee = authenticationWrapper.getAuthenticatedUserId();
        if (assignee == null) {
            throw new IllegalStateException("Assignee must be resolved from the Identity/Security Layer");
        }
        taskService.claim(taskId,
                          assignee);
        Task task = taskConverter.from(taskService.createTaskQuery().taskId(taskId).singleResult());
        return taskResourceAssembler.toResource(task);
    }

    @RequestMapping(value = "/{taskId}/release", method = RequestMethod.POST)
    public Resource<Task> releaseTask(@PathVariable String taskId) {
        taskService.unclaim(taskId);
        Task task = taskConverter.from(taskService.createTaskQuery().taskId(taskId).singleResult());
        return taskResourceAssembler.toResource(task);
    }

    @RequestMapping(value = "/{taskId}/complete", method = RequestMethod.POST)
    public ResponseEntity<Void> completeTask(@PathVariable String taskId,
                                             @RequestBody(required = false) CompleteTaskCmd completeTaskCmd) {
        Map<String, Object> inputVariables = null;
        if (completeTaskCmd != null) {
            inputVariables = completeTaskCmd.getOutputVariables();
        }
        taskService.complete(taskId,
                             inputVariables);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public AuthenticationWrapper getAuthenticationWrapper() {
        return authenticationWrapper;
    }

    public void setAuthenticationWrapper(AuthenticationWrapper authenticationWrapper) {
        this.authenticationWrapper = authenticationWrapper;
    }
}
