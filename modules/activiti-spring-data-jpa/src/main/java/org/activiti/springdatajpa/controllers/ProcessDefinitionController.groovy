package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.ProcessDefinition
import org.activiti.springdatajpa.repositories.ProcessDefinitionRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class ProcessDefinitionController extends AbstractRestController<ProcessDefinition, String> {

    ProcessDefinitionController(ProcessDefinitionRepository repo) {
        super(repo)
    }
}

