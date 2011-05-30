package org.activiti.cycle.impl.db.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.processsolution.ProcessSolutionState;
import org.activiti.engine.impl.db.PersistentObject;

/**
 * Represents a {@link ProcessSolution}
 * 
 * @author daniel.meyer@camunda.com
 */
public class ProcessSolutionEntity implements Serializable, PersistentObject, ProcessSolution {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String label;
  protected ProcessSolutionState state;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public ProcessSolutionState getState() {
    return state;
  }

  public String getStateName() {
    return state.name();
  }

  public void setStateName(String stateName) {
    this.state = ProcessSolutionState.valueOf(stateName);
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setState(ProcessSolutionState state) {
    this.state = state;
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("id", id);
    persistentState.put("label", label);
    persistentState.put("state", state.name());
    return persistentState;
  }

}
