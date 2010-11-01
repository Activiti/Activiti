package org.activiti.cycle.impl.db.entity;

import java.util.HashMap;
import java.util.Map;

import org.activiti.cycle.CycleService;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodePeopleLink;
import org.activiti.engine.impl.db.PersistentObject;

/**
 * Link between to {@link RepositoryNode}s and "People", meaning who is involved
 * or interested in this node somehow
 * 
 * @author ruecker
 */
public class RepositoryNodePeopleLinkEntity implements PersistentObject, RepositoryNodePeopleLink {
  
  /**
   * TODO: Add own mini repository for types incling names for forward and
   * reverse direction (like "is implemented by" in this case)
   */
  public static final String TYPE_CREATED = "created";
  public static final String TYPE_INTERESSTED = "interessted in";
  public static final String TYPE_INVOLVED = "involved";
  public static final String TYPE_INFORMED = "informed";
  public static final String TYPE_WATCH = "watching";
  
  /**
   * artificial id used as primary key to identify this link
   * auto generated primary key
   */
  public String id;
  
  public String sourceConnectorId;
  public String sourceArtifactId;
  
  public transient RepositoryArtifact sourceRepositoryArtifact;
  
  public Long sourceRevision;

  /**
   * Who is linked with the node?
   */
  public String userId;

  /**
   * Who is linked with the node?
   */
  public String groupId;

  /**
   * type of the link as String. Basically you could supply what you want, but
   * often it make sense to just use the supplied constants if possible
   */
  public String linkType;

  /**
   * additional description maybe supplied by the user for this link
   */
  public String comment;


  public void resolveArtifacts(CycleService service) {
    this.sourceRepositoryArtifact = service.getRepositoryArtifact(sourceConnectorId, sourceArtifactId);   
  }
  
  public void setSourceArtifact(RepositoryArtifact sourceArtifact) {
    sourceRepositoryArtifact = sourceArtifact;
    sourceConnectorId = sourceArtifact.getConnectorId();
    sourceArtifactId = sourceArtifact.getNodeId();
  }

  @Override
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("id", id);
    persistentState.put("sourceConnectorId", sourceConnectorId);
    persistentState.put("sourceArtifactId", sourceArtifactId);
    persistentState.put("sourceRevision", sourceRevision);
    persistentState.put("userId", userId);
    persistentState.put("groupId", groupId);
    persistentState.put("linkType", linkType);
    persistentState.put("comment", comment);
    return persistentState;
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSourceConnectorId() {
    return sourceConnectorId;
  }

  public void setSourceConnectorId(String sourceConnectorId) {
    this.sourceConnectorId = sourceConnectorId;
  }

  public String getSourceArtifactId() {
    return sourceArtifactId;
  }

  public void setSourceArtifactId(String sourceArtifactId) {
    this.sourceArtifactId = sourceArtifactId;
  }

  public RepositoryArtifact getSourceRepositoryArtifact() {
    return sourceRepositoryArtifact;
  }

  public void setSourceRepositoryArtifact(RepositoryArtifact sourceRepositoryArtifact) {
    this.sourceRepositoryArtifact = sourceRepositoryArtifact;
  }

  public Long getSourceRevision() {
    return sourceRevision;
  }

  public void setSourceRevision(Long sourceRevision) {
    this.sourceRevision = sourceRevision;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getLinkType() {
    return linkType;
  }

  public void setLinkType(String linkType) {
    this.linkType = linkType;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
