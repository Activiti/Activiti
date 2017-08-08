package org.activiti.services.rest.api.resources.assembler;

import org.activiti.services.core.model.ProcessDefinitionMeta;
import org.activiti.services.rest.api.HomeController;
import org.activiti.services.rest.api.ProcessDefinitionController;
import org.activiti.services.rest.api.ProcessDefinitionMetaController;
import org.activiti.services.rest.api.ProcessInstanceController;
import org.activiti.services.rest.api.resources.ProcessDefinitionMetaResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class ProcessDefinitionMetaResourceAssembler extends ResourceAssemblerSupport<ProcessDefinitionMeta, ProcessDefinitionMetaResource> {

    public ProcessDefinitionMetaResourceAssembler() {
        super(ProcessDefinitionMetaController.class,
              ProcessDefinitionMetaResource.class);
    }

    @Override
    public ProcessDefinitionMetaResource toResource(ProcessDefinitionMeta processDefinitionMeta) {

        Link metadata = linkTo(methodOn(ProcessDefinitionMetaController.class).getProcessDefinitionMetadata(processDefinitionMeta.getId())).withRel("meta");
        Link selfRel = linkTo(methodOn(ProcessDefinitionController.class).getProcessDefinition(processDefinitionMeta.getId())).withSelfRel();
        Link startProcessLink = linkTo(methodOn(ProcessInstanceController.class).startProcess(null)).withRel("startProcess");
        Link homeLink = linkTo(HomeController.class).withRel("home");

        return new ProcessDefinitionMetaResource(processDefinitionMeta,
                                                 metadata,
                                                 selfRel,
                                                 startProcessLink,
                                                 homeLink);
    }
}
