package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.IdentityLink
import org.springframework.data.jpa.repository.JpaRepository

interface IdentityLinkRepository extends JpaRepository<IdentityLink, String> {

}

