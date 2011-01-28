package org.activiti.cycle.impl.db.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.db.PersistentObject;

/**
 * Entity for holding cycle config values
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleConfigEntity implements Serializable, PersistentObject {

  private static final long serialVersionUID = -4985509539753978783L;

  private String id;

  private String groupName;

  private String key;

  private String value;

  public CycleConfigEntity() {
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("id", id);
    persistentState.put("groupName", groupName);
    persistentState.put("key", key);
    persistentState.put("value", value);
    return persistentState;
  }

  public String getId() {
    return null;
  }

  public void setId(String id) {
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
