package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.HistoricProcessInstance
import org.activiti.springdatajpa.repositories.HistoricProcessInstanceRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class HistoricProcessInstanceController extends AbstractRestController<HistoricProcessInstance, String> {

    HistoricProcessInstanceController(HistoricProcessInstanceRepository repo) {
        super(repo)
    }
}

