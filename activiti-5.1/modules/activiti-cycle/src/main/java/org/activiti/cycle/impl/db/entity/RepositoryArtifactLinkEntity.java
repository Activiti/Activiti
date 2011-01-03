package org.activiti.cycle.impl.db.entity;

import java.util.HashMap;
import java.util.Map;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.engine.impl.db.PersistentObject;

/**
 * Link between to {@link RepositoryNode}s (maybe that get extended to
 * {@link RepositoryNode}s in the future, but which is not needed at the moment.
 * 
 * The CycleLink is a persistent entity saved in the Cycle DB.
 * 
 * @author ruecker, polenz
 */
public class RepositoryArtifactLinkEntity implements PersistentObject, RepositoryArtifactLink {
  
  /**
   * TODO: Add own mini repository for types incling names for forward and
   * reverse direction (like "is implemented by" in this case)
   */
  public static final String TYPE_IMPLEMENTS = "implements";
  public static final String TYPE_REFINES = "refines";
  public static final String TYPE_UNSPECIFIED = "unspecified link";
  public static final String TYPE_COPY = "copy";
  
  /**
   * artificial id used as primary key to identify this link
   * auto generated primary key
   */
  private String id;
  
  private String sourceConnectorId;
  private String sourceArtifactId;
  
  private transient RepositoryArtifact sourceRepositoryArtifact;
  
  /**
   * machine readable id of element (what that exactly is depends on the
   * connector, could be the Signavio UUID for example)
   */
  private String sourceElementId;
  /**
   * human readable name of element (what that exactly is depends on the
   * connector, could be the Signavio Task name for example)
   */
  private String sourceElementName;
  private Long sourceRevision;
  
  private String targetConnectorId;
  private String targetArtifactId;
  
  private transient RepositoryArtifact targetRepositoryArtifact;
  
  private String targetElementId;
  private String targetElementName;
  private Long targetRevision;

  /**
   * type of the link as String. Basically you could supply what you want, but
   * often it make sense to just use the supplied constants if possible
   */
  private String linkType;

  /**
   * additional description maybe supplied by the user for this link
   */
  private String comment;

  /**
   * indicate if the link is found in both directions, default is true. If false
   * the link is only found when searching links for artifactId1
   */
  private boolean linkedBothWays = true;  

  public void resolveArtifacts(CycleRepositoryService service) {
    this.sourceRepositoryArtifact = service.getRepositoryArtifact(sourceConnectorId, sourceArtifactId);
    this.targetRepositoryArtifact = service.getRepositoryArtifact(targetConnectorId, targetArtifactId);
  }
  
  public void setSourceArtifact(RepositoryArtifact sourceArtifact) {
    sourceRepositoryArtifact = sourceArtifact;
    sourceConnectorId = sourceArtifact.getConnectorId();
    sourceArtifactId = sourceArtifact.getNodeId();
  }

  public void setTargetArtifact(RepositoryArtifact targetArtifact) {
    targetRepositoryArtifact = targetArtifact;
    targetConnectorId = targetArtifact.getConnectorId();
    targetArtifactId = targetArtifact.getNodeId();
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("id", id);
    persistentState.put("sourceConnectorId", sourceConnectorId);
    persistentState.put("sourceArtifactId", sourceArtifactId);
    persistentState.put("sourceElementId", sourceElementId);
    persistentState.put("sourceElementName", sourceElementName);
    persistentState.put("sourceRevision", sourceRevision);
    persistentState.put("targetConnectorId", targetConnectorId);
    persistentState.put("targetArtifactId", targetArtifactId);
    persistentState.put("targetElementId", targetElementId);
    persistentState.put("targetElementName", targetElementName);
    persistentState.put("targetRevision", targetRevision);
    persistentState.put("linkType", linkType);
    persistentState.put("comment", comment);
    persistentState.put("linkedBothWays", linkedBothWays);
    return persistentState;
  }
  
  public String getSourceArtifactId() {
    return sourceArtifactId;
  }

  public void setSourceArtifactId(String sourceArtifactId) {
    this.sourceArtifactId = sourceArtifactId;
  }

  public Long getSourceRevision() {
    return sourceRevision;
  }

  public void setSourceRevision(Long sourceRevision) {
    this.sourceRevision = sourceRevision;
  }

  public String getTargetArtifactId() {
    return targetArtifactId;
  }

  public void setTargetArtifactId(String targetArtifactId) {
    this.targetArtifactId = targetArtifactId;
  }

  public Long getTargetRevision() {
    return targetRevision;
  }

  public void setTargetRevision(Long targetRevision) {
    this.targetRevision = targetRevision;
  }

  public String getLinkType() {
    return linkType;
  }

  public void setLinkType(String linkType) {
    this.linkType = linkType;
  }

  
  public String getSourceElementId() {
    return sourceElementId;
  }

  public void setSourceElementId(String sourceElementId) {
    this.sourceElementId = sourceElementId;
  }

  public String getTargetElementId() {
    return targetElementId;
  }

  public void setTargetElementId(String targetElementId) {
    this.targetElementId = targetElementId;
  }

  /**
   * gets additional description maybe supplied by the user for this link
   */
  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
  
  public String getSourceElementName() {
    return sourceElementName;
  }

  public void setSourceElementName(String sourceElementName) {
    this.sourceElementName = sourceElementName;
  }

  public String getTargetElementName() {
    return targetElementName;
  }

  public void setTargetElementName(String targetElementName) {
    this.targetElementName = targetElementName;
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  
  public boolean isLinkedBothWays() {
    return linkedBothWays;
  }

  public void setLinkedBothWays(boolean linkedBothWays) {
    this.linkedBothWays = linkedBothWays;
  }
  
  public String getSourceConnectorId() {
    return sourceConnectorId;
  }
  
  public void setSourceConnectorId(String sourceConnectorId) {
    this.sourceConnectorId = sourceConnectorId;
  }

  public String getTargetConnectorId() {
    return targetConnectorId;
  }
  
  public void setTargetConnectorId(String targetConnectorId) {
    this.targetConnectorId = targetConnectorId;
  }

  public RepositoryArtifact getSourceArtifact() {
    return sourceRepositoryArtifact;
  }

  public RepositoryArtifact getTargetArtifact() {
    return targetRepositoryArtifact;
  }

  public String getGroupId() {
    return null;
  }

  public String getUserId() {
    return null;
  }

  public RepositoryArtifact getSourceRepositoryArtifact() {
    return null;
  }

}
