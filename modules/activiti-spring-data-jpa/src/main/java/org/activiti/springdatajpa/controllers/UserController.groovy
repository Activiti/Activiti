package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.User
import org.activiti.springdatajpa.repositories.UserRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController extends AbstractRestController<User, String> {

    UserController(UserRepository repo) {
        super(repo)
    }
}

