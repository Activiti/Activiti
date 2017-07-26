package org.activiti.client.model.resources.assembler;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.activiti.client.model.ProcessDefinitionMeta;
import org.activiti.client.model.resources.ProcessDefinitionMetaResource;
import org.activiti.controllers.HomeController;
import org.activiti.controllers.ProcessDefinitionController;
import org.activiti.controllers.ProcessDefinitionMetaController;
import org.activiti.controllers.ProcessInstanceController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class ProcessDefinitionMetaResourceAssembler extends ResourceAssemblerSupport<ProcessDefinitionMeta, ProcessDefinitionMetaResource> {

    public ProcessDefinitionMetaResourceAssembler() {
        super(ProcessDefinitionMetaController.class, ProcessDefinitionMetaResource.class);
    }

    @Override
    public ProcessDefinitionMetaResource toResource(ProcessDefinitionMeta processDefinitionMeta) {

        Link metadata = linkTo(methodOn(ProcessDefinitionMetaController.class).getProcessDefinitionMetadata(processDefinitionMeta.getId())).withRel("meta");
        Link selfRel = linkTo(methodOn(ProcessDefinitionController.class).getProcessDefinition(processDefinitionMeta.getId())).withSelfRel();
        Link startProcessLink = linkTo(methodOn(ProcessInstanceController.class).startProcess(null)).withRel("startProcess");
        Link homeLink = linkTo(HomeController.class).withRel("home");

        return new ProcessDefinitionMetaResource(processDefinitionMeta, metadata, selfRel, startProcessLink, homeLink);

    }

}
