package org.activiti.cycle;

/**
 * 
 * @author polenz
 */
public interface RepositoryArtifactLink {

  public String getId();
  public RepositoryArtifact getSourceArtifactId();
  public RepositoryArtifact getTargetArtifactId();
  public String getSourceElementName();
  public String getTargetElementName();
  public String getSourceElementId();
  public String getTargetElementId();
  public Long getSourceRevision();
  public Long getTargetRevision();
  public String getDescription();
  public String getLinkType();
  public Boolean isLinkedBothWays();
  
  public void setId(String id);
  public void setSourceArtifactId(RepositoryArtifact sourceArtifactId);
  public void setTargetArtifactId(RepositoryArtifact targetArtifactId);
  public void setSourceElementName(String sourceElementName);
  public void setTargetElementName(String targetElementName);
  public void setSourceElementId(String sourceElementId);
  public void setTargetElementId(String targetElementId);
  public void setSourceRevision(Long sourceRevision);
  public void setTargetRevision(Long targetRevision);
  public void setDescription(String description);
  public void setLinkType(String linkType);
  public void setLinkedBothWays(Boolean linkedBothWays);
}
