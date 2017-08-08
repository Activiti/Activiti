package org.activiti.services.rest.api;

import org.activiti.services.rest.api.resources.ProcessDefinitionMetaResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = "/v1/process-definitions/{id}/meta",
        produces = MediaTypes.HAL_JSON_VALUE)
public interface ProcessDefinitionMetaController {

    @RequestMapping(method = RequestMethod.GET)
    ProcessDefinitionMetaResource getProcessDefinitionMetadata(@PathVariable String id);
}
