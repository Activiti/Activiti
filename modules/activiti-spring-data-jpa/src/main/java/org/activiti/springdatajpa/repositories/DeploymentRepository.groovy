package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.Deployment
import org.springframework.data.jpa.repository.JpaRepository

interface DeploymentRepository extends JpaRepository<Deployment, String> {

}

