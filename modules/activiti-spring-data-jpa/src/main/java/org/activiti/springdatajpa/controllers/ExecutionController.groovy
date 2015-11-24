package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.Execution
import org.activiti.springdatajpa.repositories.ExecutionRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class ExecutionController extends AbstractRestController<Execution, String> {

    ExecutionController(ExecutionRepository repo) {
        super(repo)
    }
}

