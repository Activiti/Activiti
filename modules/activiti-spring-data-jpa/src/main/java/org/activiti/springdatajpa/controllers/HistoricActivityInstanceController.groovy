package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.HistoricActivityInstance
import org.activiti.springdatajpa.repositories.HistoricActivityInstanceRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class HistoricActivityInstanceController extends AbstractRestController<HistoricActivityInstance, String> {

    HistoricActivityInstanceController(HistoricActivityInstanceRepository repo) {
        super(repo)
    }
}

