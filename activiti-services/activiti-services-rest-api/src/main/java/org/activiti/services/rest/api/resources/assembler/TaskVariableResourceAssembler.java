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

package org.activiti.services.rest.api.resources.assembler;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.activiti.services.core.model.TaskVariables;
import org.activiti.services.core.model.TaskVariables.TaskVariableScope;
import org.activiti.services.rest.api.HomeController;
import org.activiti.services.rest.api.TaskController;
import org.activiti.services.rest.api.TaskVariableController;
import org.activiti.services.rest.api.resources.VariablesResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class TaskVariableResourceAssembler extends ResourceAssemblerSupport<TaskVariables, VariablesResource> {

    public TaskVariableResourceAssembler() {
        super(TaskVariableController.class,
              VariablesResource.class);
    }

    @Override
    public VariablesResource toResource(TaskVariables taskVariables) {
        Link selfRel;
        if (TaskVariableScope.GLOBAL.equals(taskVariables.getScope())) {
            selfRel = linkTo(methodOn(TaskVariableController.class).getVariables(taskVariables.getTaskId())).withSelfRel();
        } else {
            selfRel = linkTo(methodOn(TaskVariableController.class).getVariablesLocal(taskVariables.getTaskId())).withSelfRel();
        }
        Link taskRel = linkTo(methodOn(TaskController.class).getTaskById(taskVariables.getTaskId())).withRel("task");
        Link homeLink = linkTo(HomeController.class).withRel("home");
        return new VariablesResource(taskVariables.getVariables(),
                                     selfRel,
                                     taskRel,
                                     homeLink);
    }
}
