package org.activiti.client.model.resources.assembler;

import org.activiti.client.model.User;
import org.activiti.client.model.resources.UserResource;
import org.activiti.services.UserController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**

 */
@Component
public class UserResourceAssembler extends ResourceAssemblerSupport<User, UserResource> {

    public UserResourceAssembler() {
        super(UserController.class, UserResource.class);
    }

    @Override
    public UserResource toResource(User user) {
        Link selfRel = linkTo(methodOn(UserController.class).getUser(user.getUsername())).withSelfRel();
        return new UserResource(user, selfRel);
    }
}
