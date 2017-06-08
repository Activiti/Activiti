package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.ByteArray
import org.activiti.springdatajpa.repositories.ByteArrayRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class ByteArrayController extends AbstractRestController<ByteArray, String> {

    ByteArrayController(ByteArrayRepository repo) {
        super(repo)
    }
}

