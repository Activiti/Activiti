package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.ProcessDefinition
import org.springframework.data.jpa.repository.JpaRepository

interface ProcessDefinitionRepository extends JpaRepository<ProcessDefinition, String> {

}

