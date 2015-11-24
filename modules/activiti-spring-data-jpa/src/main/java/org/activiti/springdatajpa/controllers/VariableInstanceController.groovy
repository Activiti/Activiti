package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.VariableInstance
import org.activiti.springdatajpa.repositories.VariableInstanceRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class VariableInstanceController extends AbstractRestController<VariableInstance, String> {

    VariableInstanceController(VariableInstanceRepository repo) {
        super(repo)
    }
}

