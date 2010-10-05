package org.activiti.cycle;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.db.PersistentObject;


public class CycleConfig implements Serializable, PersistentObject {

  private static final long serialVersionUID = -4985509539753978783L;
  
  protected String id;
  protected String configXML;
  protected int revision;
  
  // default constructor
  public CycleConfig() {
  }

  //------ getter and setter ------
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getConfigXML() {
    return configXML;
  }
  
  public void setConfigXML(String configXML) {
    this.configXML = configXML;
  }
  
  public int getRevision() {
    return revision;
  }
  
  public void setRevision(int revision) {
    this.revision = revision;
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("id", id);
    persistentState.put("configXML", configXML);
    return persistentState;
  }
  
}
