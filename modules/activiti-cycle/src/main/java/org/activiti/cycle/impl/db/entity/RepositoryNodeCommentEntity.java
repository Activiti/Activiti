package org.activiti.cycle.impl.db.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.cycle.RepositoryNodeComment;
import org.activiti.engine.impl.db.PersistentObject;

public class RepositoryNodeCommentEntity implements RepositoryNodeComment, PersistentObject {

  public String id;

  public String connectorId;

  public String nodeId;

  public String elementId;

  public String content;

  public String author;

  public Date date;

  public String answeredCommentId;

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("id", id);
    persistentState.put("connectorId", connectorId);
    persistentState.put("nodeId", nodeId);
    persistentState.put("elementId", elementId);
    persistentState.put("content", content);
    persistentState.put("author", author);
    persistentState.put("date", date);
    persistentState.put("answeredCommentId", answeredCommentId);
    return persistentState;
  }

  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(String connectorId) {
    this.connectorId = connectorId;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getAnsweredCommentId() {
    return answeredCommentId;
  }

  public void setAnsweredCommentId(String answeredCommentId) {
    this.answeredCommentId = answeredCommentId;
  }
  
}
