package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.Task
import org.activiti.springdatajpa.repositories.TaskRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class TaskController extends AbstractRestController<Task, String> {

    TaskController(TaskRepository repo) {
        super(repo)
    }
}

