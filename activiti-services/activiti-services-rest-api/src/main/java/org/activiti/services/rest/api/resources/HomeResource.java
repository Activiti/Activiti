package org.activiti.services.rest.api.resources;

import org.springframework.hateoas.ResourceSupport;

public class HomeResource extends ResourceSupport {

    private final String welcome = "Welcome to an instance of the Activiti Process Engine";

    public String getWelcome() {
        return welcome;
    }
}
