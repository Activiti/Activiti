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

package org.activiti.services.rest.controllers;

import org.activiti.services.core.model.Task;
import org.activiti.services.core.pageable.PageableTaskService;
import org.activiti.services.rest.api.ProcessInstanceTasksController;
import org.activiti.services.rest.api.resources.TaskResource;
import org.activiti.services.rest.api.resources.assembler.TaskResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessInstanceTasksControllerImpl implements ProcessInstanceTasksController {

    private final PageableTaskService pageableTaskService;

    private final TaskResourceAssembler taskResourceAssembler;

    @Autowired
    public ProcessInstanceTasksControllerImpl(PageableTaskService pageableTaskService,
                                              TaskResourceAssembler taskResourceAssembler) {
        this.pageableTaskService = pageableTaskService;
        this.taskResourceAssembler = taskResourceAssembler;
    }

    @Override
    public PagedResources<TaskResource> getTasks(@PathVariable String processInstanceId,
                                                 Pageable pageable,
                                                 PagedResourcesAssembler<Task> pagedResourcesAssembler) {
        Page<Task> page = pageableTaskService.getTasks(processInstanceId,
                                                       pageable);
        return pagedResourcesAssembler.toResource(page,
                                                  taskResourceAssembler);
    }
}
