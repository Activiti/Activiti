package org.activiti.services.rest.api;

import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.core.model.commands.SignalProcessInstancesCmd;
import org.activiti.services.core.model.commands.StartProcessInstanceCmd;
import org.activiti.services.rest.api.resources.ProcessInstanceResource;
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
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping(value = "/v1/process-instances", produces = MediaTypes.HAL_JSON_VALUE)
public interface ProcessInstanceController {

    @RequestMapping(method = RequestMethod.GET)
    PagedResources<ProcessInstanceResource> getProcessInstances(Pageable pageable,
                                                                PagedResourcesAssembler<ProcessInstance> pagedResourcesAssembler);

    @RequestMapping(method = RequestMethod.POST)
    Resource<ProcessInstance> startProcess(@RequestBody StartProcessInstanceCmd cmd);

    @RequestMapping(value = "/{processInstanceId}", method = RequestMethod.GET)
    Resource<ProcessInstance> getProcessInstanceById(@PathVariable String processInstanceId);

    @RequestMapping(value = "/{processInstanceId}/svg",
            method = RequestMethod.GET,
            produces = "image/svg+xml")
    @ResponseBody
    String getProcessDiagram(@PathVariable String processInstanceId);

    @RequestMapping(value = "/signal")
    ResponseEntity<Void> sendSignal(@RequestBody SignalProcessInstancesCmd cmd);

    @RequestMapping(value = "{processInstanceId}/suspend")
    ResponseEntity<Void> suspend(@PathVariable String processInstanceId);

    @RequestMapping(value = "{processInstanceId}/activate")
    ResponseEntity<Void> activate(@PathVariable String processInstanceId);
}
