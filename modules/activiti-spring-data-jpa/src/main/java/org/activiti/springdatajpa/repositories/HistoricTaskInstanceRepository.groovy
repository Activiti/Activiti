package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.HistoricTaskInstance
import org.springframework.data.jpa.repository.JpaRepository

interface HistoricTaskInstanceRepository extends JpaRepository<HistoricTaskInstance, String> {

}

