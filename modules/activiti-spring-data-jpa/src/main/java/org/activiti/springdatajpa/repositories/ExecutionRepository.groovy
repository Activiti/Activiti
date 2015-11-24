package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.Execution
import org.springframework.data.jpa.repository.JpaRepository

interface ExecutionRepository extends JpaRepository<Execution, String> {

}

