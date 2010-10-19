package org.activiti.cycle.impl;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;

/**
 * 
 * @author polenz
 */
public class RepositoryArtifactLinkImpl implements RepositoryArtifactLink {

  private String id;
  private RepositoryArtifact sourceArtifactId;
  private RepositoryArtifact targetArtifactId;
  private String sourceElementId;
  private String targetElementId;
  private String sourceElementName;
  private String targetElementName;
  private Long sourceRevision;
  private Long targetRevision;
  private String description;
  private String linkType;
  private Boolean isLinkedBothWays;
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public RepositoryArtifact getSourceArtifactId() {
    return sourceArtifactId;
  }
  
  public void setSourceArtifactId(RepositoryArtifact sourceArtifactId) {
    this.sourceArtifactId = sourceArtifactId;
  }
  
  public RepositoryArtifact getTargetArtifactId() {
    return targetArtifactId;
  }
  
  public void setTargetArtifactId(RepositoryArtifact targetArtifactId) {
    this.targetArtifactId = targetArtifactId;
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
  
  public Long getSourceRevision() {
    return sourceRevision;
  }
  
  public void setSourceRevision(Long sourceRevision) {
    this.sourceRevision = sourceRevision;
  }
  
  public Long getTargetRevision() {
    return targetRevision;
  }
  
  public void setTargetRevision(Long targetRevision) {
    this.targetRevision = targetRevision;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getLinkType() {
    return linkType;
  }
  
  public void setLinkType(String linkType) {
    this.linkType = linkType;
  }
  
  public Boolean isLinkedBothWays() {
    return isLinkedBothWays;
  }
  
  public void setLinkedBothWays(Boolean isLinkedBothWays) {
    this.isLinkedBothWays = isLinkedBothWays;
  }
  
  
}
