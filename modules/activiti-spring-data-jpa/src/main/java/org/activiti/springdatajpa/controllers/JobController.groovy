package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.Job
import org.activiti.springdatajpa.repositories.JobRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class JobController extends AbstractRestController<Job, String> {

    JobController(JobRepository repo) {
        super(repo)
    }
}

