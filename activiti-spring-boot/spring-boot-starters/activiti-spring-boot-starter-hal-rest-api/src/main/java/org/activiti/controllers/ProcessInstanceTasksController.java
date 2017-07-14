/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.controllers;

import org.activiti.client.model.Task;
import org.activiti.client.model.resources.TaskResource;
import org.activiti.client.model.resources.assembler.TaskResourceAssembler;
import org.activiti.services.PageableTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "process-instances/{processInstanceId}", produces = MediaTypes.HAL_JSON_VALUE)
public class ProcessInstanceTasksController {

    private final PageableTaskService pageableTaskService;

    private final TaskResourceAssembler taskResourceAssembler;

    @Autowired
    public ProcessInstanceTasksController(PageableTaskService pageableTaskService,
                                          TaskResourceAssembler taskResourceAssembler) {
        this.pageableTaskService = pageableTaskService;
        this.taskResourceAssembler = taskResourceAssembler;
    }

    @RequestMapping("/tasks")
    public PagedResources<TaskResource> getTasks(@PathVariable String processInstanceId, Pageable pageable, PagedResourcesAssembler<Task> pagedResourcesAssembler) {
        Page<Task> page = pageableTaskService.getTasks(processInstanceId,
                                                        pageable);
        return pagedResourcesAssembler.toResource(page, taskResourceAssembler);
    }

}
