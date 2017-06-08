package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.HistoricProcessInstance
import org.springframework.data.jpa.repository.JpaRepository

interface HistoricProcessInstanceRepository extends JpaRepository<HistoricProcessInstance, String> {

}

