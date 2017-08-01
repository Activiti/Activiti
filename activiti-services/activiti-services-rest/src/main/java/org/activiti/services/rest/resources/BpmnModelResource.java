package org.activiti.services.rest.resources;

import org.activiti.services.core.model.MetaBpmnModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

public class BpmnModelResource extends Resource<MetaBpmnModel> {

    public BpmnModelResource(MetaBpmnModel model,
                             Link... links) {
        super(model);
    }

}
