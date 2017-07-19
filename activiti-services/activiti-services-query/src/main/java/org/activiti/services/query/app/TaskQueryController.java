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

package org.activiti.services.query.app;

import org.activiti.services.query.app.dao.TaskRepository;
import org.activiti.services.query.app.resources.TaskQueryResource;
import org.activiti.services.query.app.specification.builders.TaskSpecificationsBuilder;
import org.activiti.services.query.app.model.Task;
import org.activiti.services.query.app.assembler.TaskQueryResourceAssembler;
import org.activiti.services.query.app.util.SearchTermResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.data.jpa.domain.Specification;



@Controller
@RequestMapping(value = "/query/", produces = MediaTypes.HAL_JSON_VALUE)
public class TaskQueryController {

    @Autowired
    private TaskRepository dao;

    @Autowired
    private TaskQueryResourceAssembler taskResourceAssembler;

    @RequestMapping(method = RequestMethod.GET, value = "/tasks")
    @ResponseBody
    public PagedResources<TaskQueryResource> findAllPaginated(@RequestParam(value = "search", required = false) String search, Pageable pageable, PagedResourcesAssembler<Task> pagedResourcesAssembler) {

        Specification<Task> spec = new SearchTermResolver<Task>().applyBuilderToSearchTerm(search,new TaskSpecificationsBuilder());
        return pagedResourcesAssembler.toResource(dao.findAll(spec,pageable), taskResourceAssembler);
    }

    //TODO: implement for a single task using findOne and include links from the findAll to individual records like runtime does - see TaskResourceAssembler


}
