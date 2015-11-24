package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.Model
import org.springframework.data.jpa.repository.JpaRepository

interface ModelRepository extends JpaRepository<Model, String> {

}

