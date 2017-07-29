package org.activiti.services.rest.resources.assembler;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.activiti.services.core.model.ProcessModel;
import org.activiti.services.rest.controllers.HomeController;
import org.activiti.services.rest.controllers.ProcessDefinitionController;
import org.activiti.services.rest.controllers.ProcessInstanceController;
import org.activiti.services.rest.resources.ProcessModelResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class ProcessModelResourceAssembler extends ResourceAssemblerSupport<ProcessModel, ProcessModelResource> {

    public ProcessModelResourceAssembler() {
        super(ProcessDefinitionController.class, ProcessModelResource.class);
    }

    @Override
    public ProcessModelResource toResource(ProcessModel model) {
        Link selfRel = linkTo(methodOn(ProcessDefinitionController.class).getProcessModel(model.getId())).withSelfRel();
        Link startProcessLink = linkTo(methodOn(ProcessInstanceController.class).startProcess(null)).withRel("startProcess");
        Link homeLink = linkTo(HomeController.class).withRel("home");
        return new ProcessModelResource(model, selfRel, startProcessLink, homeLink);
    }

}
