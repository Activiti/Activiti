package org.activiti.cycle.impl.db.entity;

import java.util.HashMap;
import java.util.Map;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryNodeTag;
import org.activiti.engine.impl.db.PersistentObject;

/**
 * DB-Entity to save tag for one special {@link RepositoryArtifact}
 * 
 * @author ruecker
 */
public class RepositoryNodeTagEntity implements PersistentObject, RepositoryNodeTag {

  /**
   * primary key / unique id composed of tag name, connector id and artifact id
   */
  private String id;

  /**
   * name of the tag
   */
  private String name;
  
  private String connectorId;
  
  private String artifactId;

  /**
   * optional alias to be used in GUI when showing this element in the GUI
   */
  private String alias;

  public RepositoryNodeTagEntity() {
  }

  public RepositoryNodeTagEntity(String name, String connectorId, String artifactId) {
    this.name = name;
    this.connectorId = connectorId;
    this.artifactId = artifactId;
    createId();
  }

  public void createId() {
    // TODO: Check if this works with the Activiti persistence mechanism
    setId(name + "->" + connectorId + "/" + artifactId);
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getConnectorId() {
    return connectorId;
  }

  public String getNodeId() {
    return artifactId;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("id", id);
    persistentState.put("name", name);
    persistentState.put("connectorId", connectorId);
    persistentState.put("artifactId", artifactId);
    persistentState.put("alias", alias);
    return persistentState;
  }

  public boolean hasAlias() {
    return (alias != null && alias.length() > 0);
  }

  public String getComment() {
    return null;
  }

  public String getLinkType() {
    return null;
  }

  public String getGroupId() {
    return null;
  }

  public String getUserId() {
    return null;
  }

  public Long getSourceRevision() {
    return null;
  }

  public RepositoryArtifact getSourceRepositoryArtifact() {
    return null;
  }

  public String getSourceArtifactId() {
    return null;
  }

  public String getSourceConnectorId() {
    return null;
  }

}
