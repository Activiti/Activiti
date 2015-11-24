package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.VariableInstance
import org.springframework.data.jpa.repository.JpaRepository

interface VariableInstanceRepository extends JpaRepository<VariableInstance, String> {

}

