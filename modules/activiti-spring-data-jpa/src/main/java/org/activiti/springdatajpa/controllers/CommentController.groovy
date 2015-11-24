package org.activiti.springdatajpa.controllers

import org.activiti.springdatajpa.AbstractRestController
import org.activiti.springdatajpa.models.Comment
import org.activiti.springdatajpa.repositories.CommentRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentController extends AbstractRestController<Comment, String> {

    CommentController(CommentRepository repo) {
        super(repo)
    }
}

