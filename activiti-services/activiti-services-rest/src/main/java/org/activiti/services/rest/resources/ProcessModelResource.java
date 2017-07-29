package org.activiti.services.rest.resources;

import org.activiti.services.core.model.ProcessModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

public class ProcessModelResource extends Resource<ProcessModel> {

    public ProcessModelResource(ProcessModel content,
                                Link... links) {
        super(content,
              links);
    }
}
