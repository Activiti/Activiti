package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.ProcessDefinitionInfo
import org.springframework.data.jpa.repository.JpaRepository

interface ProcessDefinitionInfoRepository extends JpaRepository<ProcessDefinitionInfo, String> {

}

