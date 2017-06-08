package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.IdentityInfo
import org.activiti.springdatajpa.repositories.IdentityInfoRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class IdentityInfoController extends AbstractRestController<IdentityInfo, String> {

    IdentityInfoController(IdentityInfoRepository repo) {
        super(repo)
    }
}

