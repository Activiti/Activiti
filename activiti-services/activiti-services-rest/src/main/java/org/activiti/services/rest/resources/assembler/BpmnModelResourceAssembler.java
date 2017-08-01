package org.activiti.services.rest.resources.assembler;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.activiti.services.core.model.MetaBpmnModel;
import org.activiti.services.rest.controllers.HomeController;
import org.activiti.services.rest.controllers.ProcessDefinitionController;
import org.activiti.services.rest.controllers.ProcessInstanceController;
import org.activiti.services.rest.resources.BpmnModelResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class BpmnModelResourceAssembler extends ResourceAssemblerSupport<MetaBpmnModel, BpmnModelResource> {

    public BpmnModelResourceAssembler() {
        super(ProcessDefinitionController.class, BpmnModelResource.class);
    }

    @Override
    public BpmnModelResource toResource(MetaBpmnModel model) {
        Link selfRel = linkTo(methodOn(ProcessDefinitionController.class).getBpmnModel(model.getId())).withSelfRel();
        Link startProcessLink = linkTo(methodOn(ProcessInstanceController.class).startProcess(null)).withRel("startProcess");
        Link homeLink = linkTo(HomeController.class).withRel("home");
        return new BpmnModelResource(model, selfRel, startProcessLink, homeLink);
    }

}
