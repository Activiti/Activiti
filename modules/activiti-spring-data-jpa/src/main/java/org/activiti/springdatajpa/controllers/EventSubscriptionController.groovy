package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.EventSubscription
import org.activiti.springdatajpa.repositories.EventSubscriptionRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class EventSubscriptionController extends AbstractRestController<EventSubscription, String> {

    EventSubscriptionController(EventSubscriptionRepository repo) {
        super(repo)
    }
}

