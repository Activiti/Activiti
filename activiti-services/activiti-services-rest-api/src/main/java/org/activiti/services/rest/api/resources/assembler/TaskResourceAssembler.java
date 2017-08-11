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

package org.activiti.services.rest.api.resources.assembler;

import java.util.ArrayList;
import java.util.List;

import org.activiti.services.core.model.Task;
import org.activiti.services.rest.api.HomeController;
import org.activiti.services.rest.api.ProcessInstanceController;
import org.activiti.services.rest.api.TaskController;
import org.activiti.services.rest.api.resources.TaskResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class TaskResourceAssembler extends ResourceAssemblerSupport<Task, TaskResource> {

    public TaskResourceAssembler() {
        super(TaskController.class,
              TaskResource.class);
    }

    @Override
    public TaskResource toResource(Task task) {
        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(TaskController.class).getTaskById(task.getId())).withSelfRel());
        if (!task.getStatus().equals(Task.TaskStatus.ASSIGNED.name())) {
            links.add(linkTo(methodOn(TaskController.class).claimTask(task.getId())).withRel("claim"));
        } else {
            links.add(linkTo(methodOn(TaskController.class).releaseTask(task.getId())).withRel("release"));
            links.add(linkTo(methodOn(TaskController.class).completeTask(task.getId(),
                                                                         null)).withRel("complete"));
        }
        links.add(linkTo(methodOn(ProcessInstanceController.class).getProcessInstanceById(task.getProcessInstanceId())).withRel("processInstance"));
        if (task.getParentTaskId() != null && !task.getParentTaskId().isEmpty()) {
            links.add(linkTo(methodOn(TaskController.class).getTaskById(task.getParentTaskId())).withRel("parent"));
        }
        links.add(linkTo(HomeController.class).withRel("home"));
        return new TaskResource(task,
                                links);
    }
}
