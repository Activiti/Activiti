package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.ByteArray
import org.springframework.data.jpa.repository.JpaRepository

interface ByteArrayRepository extends JpaRepository<ByteArray, String> {

}

