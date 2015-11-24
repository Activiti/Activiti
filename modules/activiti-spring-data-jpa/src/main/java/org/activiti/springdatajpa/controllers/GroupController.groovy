package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.Group
import org.activiti.springdatajpa.repositories.GroupRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class GroupController extends AbstractRestController<Group, String> {

    GroupController(GroupRepository repo) {
        super(repo)
    }
}

