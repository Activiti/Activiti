package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.Group
import org.springframework.data.jpa.repository.JpaRepository

interface GroupRepository extends JpaRepository<Group, String> {

}

