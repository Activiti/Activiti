package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.Attachment
import org.springframework.data.jpa.repository.JpaRepository

interface AttachmentRepository extends JpaRepository<Attachment, String> {

}

