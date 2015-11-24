package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.HistoricVariableInstance
import org.springframework.data.jpa.repository.JpaRepository

interface HistoricVariableInstanceRepository extends JpaRepository<HistoricVariableInstance, String> {

}

