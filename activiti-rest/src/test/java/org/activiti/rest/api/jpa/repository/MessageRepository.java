package org.activiti.rest.api.jpa.repository;

import org.activiti.rest.api.jpa.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MessageRepository extends JpaRepository<Message, Long> {

}