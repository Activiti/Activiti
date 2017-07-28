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

package org.activiti.services.query.app.controller;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.activiti.services.query.app.assembler.ProcessInstanceQueryResourceAssembler;
import org.activiti.services.query.app.model.ProcessInstance;

import org.activiti.services.query.app.model.QProcessInstance;
import org.activiti.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.services.query.app.resource.ProcessInstanceQueryResource;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/query/processinstances", produces = MediaTypes.HAL_JSON_VALUE)
public class ProcessInstanceQueryController {

    @Autowired
    private ProcessInstanceRepository dao;

    @Autowired
    private ProcessInstanceQueryResourceAssembler resourceAssembler;


    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public PagedResources<ProcessInstanceQueryResource> findAllByWebQuerydsl(
            @QuerydslPredicate(root = ProcessInstance.class) Predicate predicate, Pageable pageable, PagedResourcesAssembler<ProcessInstance> pagedResourcesAssembler) {
        return pagedResourcesAssembler.toResource(dao.findAll(predicate,pageable), resourceAssembler);
    }

    //this shows that we can add an OR condition to a query with an extra parameter
    //it could replace the basic processinstances endpoint but instead put it on new endpoint as it's only a POC without real value
    @RequestMapping(method = RequestMethod.GET, value = "or")
    public PagedResources<ProcessInstanceQueryResource> findAllByWebQuerydslWithOr(
            @QuerydslPredicate(root = ProcessInstance.class) Predicate predicate, @RequestParam(value = "orStatus", required = false) String orStatus, Pageable pageable, PagedResourcesAssembler<ProcessInstance> pagedResourcesAssembler) {

        //could maybe use ExpressionUtils.anyOf but BooleanBuilder is simpler - see http://www.querydsl.com/static/querydsl/3.2.0/apidocs/com/mysema/query/BooleanBuilder.html

        if(orStatus!=null && predicate!=null) {
            //if the OR condition is specified then we add it to the predicate
            BooleanBuilder builder = new BooleanBuilder();
            QProcessInstance qProcessInstance = QProcessInstance.processInstance;
            predicate = qProcessInstance.status.eq(orStatus).or(predicate);
        } //don't handle case where OR is provided without predicate as doesn't make sense unless you've something to OR against

        return pagedResourcesAssembler.toResource(dao.findAll(predicate,pageable), resourceAssembler);
    }

    //TODO: in addition to above for ORs we can also use transient attributes as suggested in https://stackoverflow.com/questions/35155824/can-spring-data-rests-querydsl-integration-be-used-to-perform-more-complex-quer
    // and we can combine this with in, contains, notIn, like etc. in the path bindings in the customizer of the repository


    @RequestMapping(value = "/{processInstanceId}", method = RequestMethod.GET)
    public Resource<ProcessInstance> getProcessInstanceById(@PathVariable Long processInstanceId) {
        return resourceAssembler.toResource(dao.findById(processInstanceId).get());
    }
}