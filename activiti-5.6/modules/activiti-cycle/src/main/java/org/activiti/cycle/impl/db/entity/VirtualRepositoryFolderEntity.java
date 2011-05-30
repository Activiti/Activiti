package org.activiti.cycle.impl.db.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.engine.impl.db.PersistentObject;

/**
 * Represents a {@link VirtualRepositoryFolder}.
 * 
 * @author daniel.meyer@camunda.com
 */
public class VirtualRepositoryFolderEntity implements VirtualRepositoryFolder, Serializable, PersistentObject {

  private static final long serialVersionUID = 1L;

  private String id;

  private String label;

  private String connectorId;

  private String referencedNodeId;

  private String processSolutionId;

  private String type;

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("id", id);
    persistentState.put("label", label);
    persistentState.put("connectorId", connectorId);
    persistentState.put("processSolutionId", processSolutionId);
    persistentState.put("type", type);
    persistentState.put("referencedNodeId", referencedNodeId);
    return persistentState;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(String connectorId) {
    this.connectorId = connectorId;
  }

  public String getReferencedNodeId() {
    return referencedNodeId;
  }

  public void setReferencedNodeId(String referencedNodeId) {
    this.referencedNodeId = referencedNodeId;
  }

  public String getProcessSolutionId() {
    return processSolutionId;
  }

  public void setProcessSolutionId(String processSolutionId) {
    this.processSolutionId = processSolutionId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

}
