package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.HistoricDetail
import org.activiti.springdatajpa.repositories.HistoricDetailRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class HistoricDetailController extends AbstractRestController<HistoricDetail, String> {

    HistoricDetailController(HistoricDetailRepository repo) {
        super(repo)
    }
}

