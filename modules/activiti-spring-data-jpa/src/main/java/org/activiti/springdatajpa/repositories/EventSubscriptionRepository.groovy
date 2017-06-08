package org.activiti.springdatajpa.repositories

import org.activiti.springdatajpa.models.EventSubscription
import org.springframework.data.jpa.repository.JpaRepository

interface EventSubscriptionRepository extends JpaRepository<EventSubscription, String> {

}

