package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.HistoricIdentityLink
import org.springframework.data.jpa.repository.JpaRepository

interface HistoricIdentityLinkRepository extends JpaRepository<HistoricIdentityLink, String> {

}

