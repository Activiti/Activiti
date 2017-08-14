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

package org.activiti.services.query.controller;

import com.querydsl.core.types.Predicate;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.services.query.app.model.QTask;
import org.activiti.services.query.app.model.Task;
import org.activiti.services.query.app.repository.EntityFinder;
import org.activiti.services.query.app.repository.TaskRepository;
import org.activiti.services.query.assembler.TaskQueryResourceAssembler;
import org.activiti.services.query.resource.TaskQueryResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/tasks", produces = MediaTypes.HAL_JSON_VALUE)
public class TaskQueryController {

    private final TaskRepository dao;

    private final EntityFinder entityFinder;

    private final TaskQueryResourceAssembler resourceAssembler;

    @Autowired
    public TaskQueryController(TaskRepository dao,
                               EntityFinder entityFinder,
                               TaskQueryResourceAssembler resourceAssembler) {
        this.dao = dao;
        this.entityFinder = entityFinder;
        this.resourceAssembler = resourceAssembler;
    }

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<TaskQueryResource> findAllByWebQuerydsl(
            @QuerydslPredicate(root = Task.class) Predicate predicate,
            Pageable pageable,
            PagedResourcesAssembler<Task> pagedResourcesAssembler) {
        return pagedResourcesAssembler.toResource(dao.findAll(predicate,
                                                              pageable),
                                                  resourceAssembler);
    }

    @RequestMapping(value = "/{taskId}", method = RequestMethod.GET)
    public Resource<Task> getTaskById(@PathVariable String taskId) {
        return resourceAssembler.toResource(entityFinder.findById(dao,
                                                                  taskId,
                                                                  "Unable to find task: " + taskId));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/assignedToMe")
    public PagedResources<TaskQueryResource> findAllAssignedToMe(
            @QuerydslPredicate(root = Task.class) Predicate predicate,
            Pageable pageable,
            PagedResourcesAssembler<Task> pagedResourcesAssembler) {

        String authenticatedUser = Authentication.getAuthenticatedUserId();
        //TODO: authenticatedUser is always null, despite being set by KeycloakActivitiAuthenticationProvider
        // why does it work for sample-hal-rest-api and not for this?
        // could go to spring security context instead?

        if (authenticatedUser != null) {
            QTask qTask = QTask.task;
            predicate = qTask.assignee.eq(authenticatedUser).and(predicate);
        }

        return pagedResourcesAssembler.toResource(dao.findAll(predicate,
                                                              pageable),
                                                  resourceAssembler);
    }
}