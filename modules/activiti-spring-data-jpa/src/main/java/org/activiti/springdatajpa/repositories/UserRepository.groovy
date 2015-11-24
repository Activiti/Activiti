package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository extends JpaRepository<User, String> {

}

