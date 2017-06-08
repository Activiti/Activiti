package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.Model
import org.activiti.springdatajpa.repositories.ModelRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class ModelController extends AbstractRestController<Model, String> {

    ModelController(ModelRepository repo) {
        super(repo)
    }
}

