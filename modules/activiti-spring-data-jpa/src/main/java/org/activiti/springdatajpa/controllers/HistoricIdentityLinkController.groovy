package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.HistoricIdentityLink
import org.activiti.springdatajpa.repositories.HistoricIdentityLinkRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class HistoricIdentityLinkController extends AbstractRestController<HistoricIdentityLink, String> {

    HistoricIdentityLinkController(HistoricIdentityLinkRepository repo) {
        super(repo)
    }
}

