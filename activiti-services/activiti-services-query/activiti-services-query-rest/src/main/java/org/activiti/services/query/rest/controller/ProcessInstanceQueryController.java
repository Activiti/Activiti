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

package org.activiti.services.query.rest.controller;

import org.activiti.services.query.es.model.ProcessInstanceES;
import org.activiti.services.query.es.repository.ProcessInstanceRepository;
import org.activiti.services.query.rest.assembler.ProcessInstanceQueryResourceAssembler;
import org.activiti.services.query.resource.ProcessInstanceQueryResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/process-instances", produces = MediaTypes.HAL_JSON_VALUE)
public class ProcessInstanceQueryController {

	private final ProcessInstanceRepository processInstanceRepository;

	private final ProcessInstanceQueryResourceAssembler resourceAssembler;

	@Autowired
	public ProcessInstanceQueryController(ProcessInstanceRepository processInstanceRepository,
			ProcessInstanceQueryResourceAssembler resourceAssembler) {
		this.processInstanceRepository = processInstanceRepository;
		this.resourceAssembler = resourceAssembler;
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public PagedResources<ProcessInstanceQueryResource> findAll(Pageable pageable,
			PagedResourcesAssembler<ProcessInstanceES> pagedResourcesAssembler) {
		return pagedResourcesAssembler.toResource(processInstanceRepository.findAll(pageable), resourceAssembler);
	}

	@RequestMapping(value = "/{processInstanceId}", method = RequestMethod.GET)
	public Resource<ProcessInstanceES> getProcessInstanceById(@PathVariable Long processInstanceId) {
		return resourceAssembler.toResource(processInstanceRepository.findById(processInstanceId));
	}
}