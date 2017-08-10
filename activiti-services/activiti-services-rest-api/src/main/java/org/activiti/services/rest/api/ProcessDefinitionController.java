package org.activiti.services.rest.api;

import org.activiti.services.core.model.ProcessDefinition;
import org.activiti.services.rest.api.resources.ProcessDefinitionResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping(value = "/v1/process-definitions",
        produces = MediaTypes.HAL_JSON_VALUE)
public interface ProcessDefinitionController {

    @RequestMapping(method = RequestMethod.GET)
    PagedResources<ProcessDefinitionResource> getProcessDefinitions(Pageable pageable,
                                                                    PagedResourcesAssembler<ProcessDefinition> pagedResourcesAssembler);

    @RequestMapping(value = "/{id}",
            method = RequestMethod.GET)
    ProcessDefinitionResource getProcessDefinition(@PathVariable String id);

    @RequestMapping(value = "/{id}/xml",
            method = RequestMethod.GET,
            produces = "application/xml")
    @ResponseBody
    String getProcessModel(@PathVariable String id);

    @RequestMapping(value = "/{id}/json",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    String getBpmnModel(@PathVariable String id);

    @RequestMapping(value = "/{id}/svg",
            method = RequestMethod.GET,
            produces = "image/svg+xml")
    @ResponseBody
    String getProcessDiagram(@PathVariable String id);
}
