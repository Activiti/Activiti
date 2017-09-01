
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

package org.activiti.services.query.rest.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.activiti.services.query.es.model.ProcessInstanceES;
import org.activiti.services.query.resource.ProcessInstanceQueryResource;
import org.activiti.services.query.rest.controller.ProcessInstanceQueryController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class ProcessInstanceQueryResourceAssembler
		extends ResourceAssemblerSupport<ProcessInstanceES, ProcessInstanceQueryResource> {

	public ProcessInstanceQueryResourceAssembler() {
		super(ProcessInstanceQueryController.class, ProcessInstanceQueryResource.class);
	}

	@Override
	public ProcessInstanceQueryResource toResource(ProcessInstanceES processInstance) {
		List<Link> links = new ArrayList<>();
		links.add(linkTo(methodOn(ProcessInstanceQueryController.class)
				.getProcessInstanceById(processInstance.getProcessInstanceId())).withSelfRel());
		return new ProcessInstanceQueryResource(processInstance, links);
	}

	public ProcessInstanceQueryResource toResource(Optional<ProcessInstanceES> optional) {
		ProcessInstanceQueryResource processInstanceQueryResource = null;
		if (optional.isPresent()) {
			ProcessInstanceES processInstance = optional.get();
			List<Link> links = new ArrayList<>();
			links.add(linkTo(methodOn(ProcessInstanceQueryController.class)
					.getProcessInstanceById(processInstance.getProcessInstanceId())).withSelfRel());
			processInstanceQueryResource = new ProcessInstanceQueryResource(processInstance, links);
		}
		return processInstanceQueryResource;
	}
}