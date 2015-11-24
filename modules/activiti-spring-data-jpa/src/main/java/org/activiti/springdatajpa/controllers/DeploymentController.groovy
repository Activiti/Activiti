package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.Deployment
import org.activiti.springdatajpa.repositories.DeploymentRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class DeploymentController extends AbstractRestController<Deployment, String> {

    DeploymentController(DeploymentRepository repo) {
        super(repo)
    }
}

