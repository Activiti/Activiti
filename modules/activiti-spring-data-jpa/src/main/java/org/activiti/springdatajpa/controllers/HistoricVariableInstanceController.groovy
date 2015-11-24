package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.HistoricVariableInstance
import org.activiti.springdatajpa.repositories.HistoricVariableInstanceRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class HistoricVariableInstanceController extends AbstractRestController<HistoricVariableInstance, String> {

    HistoricVariableInstanceController(HistoricVariableInstanceRepository repo) {
        super(repo)
    }
}

