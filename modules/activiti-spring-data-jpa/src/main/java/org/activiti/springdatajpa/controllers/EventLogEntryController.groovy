package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.EventLogEntry
import org.activiti.springdatajpa.repositories.EventLogEntryRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class EventLogEntryController extends AbstractRestController<EventLogEntry, String> {

    EventLogEntryController(EventLogEntryRepository repo) {
        super(repo)
    }
}

