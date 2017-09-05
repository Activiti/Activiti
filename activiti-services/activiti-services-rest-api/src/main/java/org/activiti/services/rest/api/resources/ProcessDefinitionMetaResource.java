package org.activiti.services.rest.api.resources;

import org.activiti.services.core.model.ProcessDefinitionMeta;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

public class ProcessDefinitionMetaResource extends Resource<ProcessDefinitionMeta> {

    public ProcessDefinitionMetaResource(ProcessDefinitionMeta content, Link... links) {
        super(content, links);
    }

}
