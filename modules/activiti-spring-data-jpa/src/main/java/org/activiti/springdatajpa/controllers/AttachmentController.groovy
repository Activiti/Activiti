package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.Attachment
import org.activiti.springdatajpa.repositories.AttachmentRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class AttachmentController extends AbstractRestController<Attachment, String> {

    AttachmentController(AttachmentRepository repo) {
        super(repo)
    }
}

