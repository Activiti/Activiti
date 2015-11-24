package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.EventLogEntry
import org.springframework.data.jpa.repository.JpaRepository

interface EventLogEntryRepository extends JpaRepository<EventLogEntry, String> {

}

