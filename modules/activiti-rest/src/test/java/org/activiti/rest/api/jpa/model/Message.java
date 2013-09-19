package org.activiti.rest.api.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "message")
public class Message {

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  @Column(name = "id", unique=true, nullable=false)
  private Long id;
  
  private String text;
  
  public Long getId() {
    return id;
  }

  public String getText() {
    return text;
  }

}