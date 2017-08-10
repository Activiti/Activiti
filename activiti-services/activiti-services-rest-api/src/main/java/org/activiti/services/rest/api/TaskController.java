package org.activiti.services.rest.api;

import org.activiti.services.core.model.Task;
import org.activiti.services.core.model.commands.CompleteTaskCmd;
import org.activiti.services.rest.api.resources.TaskResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = "/v1/tasks", produces = MediaTypes.HAL_JSON_VALUE)
public interface TaskController {

    @RequestMapping(method = RequestMethod.GET)
    PagedResources<TaskResource> getTasks(Pageable pageable,
                                          PagedResourcesAssembler<Task> pagedResourcesAssembler);

    @RequestMapping(value = "/{taskId}", method = RequestMethod.GET)
    Resource<Task> getTaskById(@PathVariable String taskId);

    @RequestMapping(value = "/{taskId}/claim", method = RequestMethod.POST)
    Resource<Task> claimTask(@PathVariable String taskId);

    @RequestMapping(value = "/{taskId}/release", method = RequestMethod.POST)
    Resource<Task> releaseTask(@PathVariable String taskId);

    @RequestMapping(value = "/{taskId}/complete", method = RequestMethod.POST)
    ResponseEntity<Void> completeTask(@PathVariable String taskId,
                                      @RequestBody(required = false) CompleteTaskCmd completeTaskCmd);
}
