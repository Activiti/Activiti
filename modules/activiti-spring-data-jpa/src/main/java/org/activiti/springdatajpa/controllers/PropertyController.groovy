package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.Property
import org.activiti.springdatajpa.repositories.PropertyRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class PropertyController extends AbstractRestController<Property, String> {

    PropertyController(PropertyRepository repo) {
        super(repo)
    }
}

