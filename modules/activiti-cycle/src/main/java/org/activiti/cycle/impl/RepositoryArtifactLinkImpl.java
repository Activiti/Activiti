package org.activiti.cycle.impl;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;

/**
 * 
 * @author polenz
 */
public class RepositoryArtifactLinkImpl implements RepositoryArtifactLink {

  private String id;

  private RepositoryArtifact sourceArtifact;
  private String sourceElementId;
  private String sourceElementName;

  private RepositoryArtifact targetArtifact;
  private String targetElementId;
  private String targetElementName;

  public RepositoryArtifactLinkImpl() {

  }

  public RepositoryArtifactLinkImpl(String id, RepositoryArtifact sourceArtifact, String sourceElementId, String sourceElementName,
          RepositoryArtifact targetArtifact, String targetElementId, String targetElementName) {
    super();
    this.id = id;
    this.sourceArtifact = sourceArtifact;
    this.sourceElementId = sourceElementId;
    this.sourceElementName = sourceElementName;
    this.targetArtifact = targetArtifact;
    this.targetElementId = targetElementId;
    this.targetElementName = targetElementName;
  }

  public String getId() {
    return id;
  }

  public RepositoryArtifact getSourceArtifact() {
    return sourceArtifact;
  }

  public String getSourceElementId() {
    return sourceElementId;
  }

  public String getSourceElementName() {
    return sourceElementName;
  }

  public RepositoryArtifact getTargetArtifact() {
    return targetArtifact;
  }

  public String getTargetElementId() {
    return targetElementId;
  }

  public String getTargetElementName() {
    return targetElementName;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setSourceArtifact(RepositoryArtifact sourceArtifact) {
    this.sourceArtifact = sourceArtifact;
  }

  public void setSourceElementId(String sourceElementId) {
    this.sourceElementId = sourceElementId;
  }

  public void setSourceElementName(String sourceElementName) {
    this.sourceElementName = sourceElementName;
  }

  public void setTargetArtifact(RepositoryArtifact targetArtifact) {
    this.targetArtifact = targetArtifact;
  }

  public void setTargetElementId(String targetElementId) {
    this.targetElementId = targetElementId;
  }

  public void setTargetElementName(String targetElementName) {
    this.targetElementName = targetElementName;
  }

}
