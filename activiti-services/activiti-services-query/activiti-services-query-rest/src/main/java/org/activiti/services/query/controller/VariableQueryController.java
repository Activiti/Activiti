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
import org.activiti.services.query.assembler.VariableQueryResourceAssembler;
import org.activiti.services.query.app.model.Variable;
import org.activiti.services.query.app.repository.EntityFinder;
import org.activiti.services.query.app.repository.VariableRepository;
import org.activiti.services.query.resource.VariableQueryResource;
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
@RequestMapping(value = "/v1/variables", produces = MediaTypes.HAL_JSON_VALUE)
public class VariableQueryController {

    private final VariableRepository variableRepository;

    private final VariableQueryResourceAssembler resourceAssembler;

    private EntityFinder entityFinder;

    @Autowired
    public VariableQueryController(VariableRepository variableRepository,
                                   VariableQueryResourceAssembler resourceAssembler,
                                   EntityFinder entityFinder) {
        this.variableRepository = variableRepository;
        this.resourceAssembler = resourceAssembler;
        this.entityFinder = entityFinder;
    }

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<VariableQueryResource> findAllByWebQuerydsl(
            @QuerydslPredicate(root = Variable.class) Predicate predicate, Pageable pageable, PagedResourcesAssembler<Variable> pagedResourcesAssembler) {
        return pagedResourcesAssembler.toResource(variableRepository.findAll(predicate, pageable), resourceAssembler);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Resource<Variable> getVariableById(@PathVariable long id) {
        return resourceAssembler.toResource(entityFinder.findById(variableRepository, id, "Unable to find variable with id: " + id));
    }

}