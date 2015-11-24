package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.HistoricDetail
import org.springframework.data.jpa.repository.JpaRepository

interface HistoricDetailRepository extends JpaRepository<HistoricDetail, String> {

}

