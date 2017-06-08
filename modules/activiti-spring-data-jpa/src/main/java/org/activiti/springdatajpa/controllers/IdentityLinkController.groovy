package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.IdentityLink
import org.activiti.springdatajpa.repositories.IdentityLinkRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class IdentityLinkController extends AbstractRestController<IdentityLink, String> {

    IdentityLinkController(IdentityLinkRepository repo) {
        super(repo)
    }
}

