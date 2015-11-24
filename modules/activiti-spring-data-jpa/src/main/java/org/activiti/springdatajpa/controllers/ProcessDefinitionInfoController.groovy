package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.ProcessDefinitionInfo
import org.activiti.springdatajpa.repositories.ProcessDefinitionInfoRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class ProcessDefinitionInfoController extends AbstractRestController<ProcessDefinitionInfo, String> {

    ProcessDefinitionInfoController(ProcessDefinitionInfoRepository repo) {
        super(repo)
    }
}

