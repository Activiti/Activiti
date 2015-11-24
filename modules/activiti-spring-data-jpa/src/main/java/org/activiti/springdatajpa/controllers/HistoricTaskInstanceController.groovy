package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.HistoricTaskInstance
import org.activiti.springdatajpa.repositories.HistoricTaskInstanceRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class HistoricTaskInstanceController extends AbstractRestController<HistoricTaskInstance, String> {

    HistoricTaskInstanceController(HistoricTaskInstanceRepository repo) {
        super(repo)
    }
}

