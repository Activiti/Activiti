package org.activiti.services.rest.api;

import org.activiti.services.core.model.Task;
import org.activiti.services.rest.api.resources.TaskResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/v1/process-instances/{processInstanceId}", produces = MediaTypes.HAL_JSON_VALUE)
public interface ProcessInstanceTasksController {

    @RequestMapping("/tasks")
    PagedResources<TaskResource> getTasks(@PathVariable String processInstanceId,
                                          Pageable pageable,
                                          PagedResourcesAssembler<Task> pagedResourcesAssembler);
}
