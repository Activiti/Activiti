package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.IdentityInfo
import org.springframework.data.jpa.repository.JpaRepository

interface IdentityInfoRepository extends JpaRepository<IdentityInfo, String> {

}

