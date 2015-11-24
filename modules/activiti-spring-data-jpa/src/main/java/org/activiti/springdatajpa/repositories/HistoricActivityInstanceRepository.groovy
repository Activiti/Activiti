package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.HistoricActivityInstance
import org.springframework.data.jpa.repository.JpaRepository

interface HistoricActivityInstanceRepository extends JpaRepository<HistoricActivityInstance, String> {

}

