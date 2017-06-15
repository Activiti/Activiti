/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.client.model.resources.assembler;

import java.util.ArrayList;
import java.util.List;

import org.activiti.client.model.Task;
import org.activiti.client.model.resources.TaskResource;
import org.activiti.services.ProcessInstanceController;
import org.activiti.services.TaskController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Elias Ricken de Medeiros
 */
@Component
public class TaskResourceAssembler extends ResourceAssemblerSupport<Task, TaskResource> {

    public TaskResourceAssembler() {
        super(TaskController.class, TaskResource.class);
    }

    @Override
    public TaskResource toResource(Task task) {
        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(TaskController.class).getTask(task.getId())).withSelfRel());
        if (!task.isClaimed()) {
            links.add(linkTo(methodOn(TaskController.class).claimTask(task.getId(), null)).withRel("claim"));
        } else {
            links.add(linkTo(methodOn(TaskController.class).completeTask(task.getId(), null)).withRel("complete"));
        }
        links.add(linkTo(methodOn(ProcessInstanceController.class).getProcessInstance(task.getProcessInstanceId())).withRel("processInstance"));
        return new TaskResource(task, links);
    }
}
