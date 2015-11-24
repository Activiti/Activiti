package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.Comment
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository extends JpaRepository<Comment, String> {

}

