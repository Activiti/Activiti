package org.activiti.form.engine.impl.persistence;

public class IdBlock {

  long nextId;
  long lastId;

  public IdBlock(long nextId, long lastId) {
    this.nextId = nextId;
    this.lastId = lastId;
  }

  public long getNextId() {
    return nextId;
  }

  public long getLastId() {
    return lastId;
  }
}