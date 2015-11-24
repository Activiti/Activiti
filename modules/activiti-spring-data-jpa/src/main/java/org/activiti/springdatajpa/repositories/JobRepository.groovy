package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.Job
import org.springframework.data.jpa.repository.JpaRepository

interface JobRepository extends JpaRepository<Job, String> {

}

