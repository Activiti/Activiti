package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.Task
import org.springframework.data.jpa.repository.JpaRepository

interface TaskRepository extends JpaRepository<Task, String> {

}

