package org.activiti.services.rest.resources;

import org.springframework.hateoas.ResourceSupport;

public class HomeResource extends ResourceSupport{
    private String welcome = "Welcome to an instance of the Activiti Process Engine";

    public HomeResource() {
    }

    public String getWelcome() {
        return welcome;
    }
}
