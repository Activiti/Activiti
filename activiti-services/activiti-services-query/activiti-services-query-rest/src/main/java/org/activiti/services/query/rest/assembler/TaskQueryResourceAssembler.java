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

import org.activiti.services.query.rest.controller.TaskQueryController;
import org.activiti.services.query.es.model.TaskES;
import org.activiti.services.query.resource.TaskQueryResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class TaskQueryResourceAssembler extends ResourceAssemblerSupport<TaskES, TaskQueryResource> {

	public TaskQueryResourceAssembler() {
		super(TaskQueryController.class, TaskQueryResource.class);
	}

	@Override
	public TaskQueryResource toResource(TaskES task) {
		List<Link> links = new ArrayList<>();
		links.add(linkTo(methodOn(TaskQueryController.class).getTaskById(task.getId())).withSelfRel());
		return new TaskQueryResource(task, links);
	}

	public TaskQueryResource toResource(Optional<TaskES> optional) {
		TaskQueryResource taskQueryResource = null;
		if (optional.isPresent()) {
			TaskES task = optional.get();
			List<Link> links = new ArrayList<>();
			links.add(linkTo(methodOn(TaskQueryController.class).getTaskById(task.getId())).withSelfRel());
			taskQueryResource = new TaskQueryResource(task, links);
		}

		return taskQueryResource;
	}

}