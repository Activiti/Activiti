package org.activiti.cycle;

import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkImpl;

/**
 * Object given back by the {@link CycleService} for a {@link RepositoryArtifactLinkImpl} where
 * the {@link RepositoryArtifact}s are already resolved.
 * 
 * @author polenz
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public interface RepositoryArtifactLink {

  public String getId();
  public RepositoryArtifact getSourceArtifact();
  public String getSourceElementId();
  public String getSourceElementName();
  public RepositoryArtifact getTargetArtifact();
  public String getTargetElementId();
  public String getTargetElementName();
  public String getComment();
  public String getLinkType();
  
  public void setId(String id);
  public void setSourceArtifact(RepositoryArtifact sourceArtifact);
  public void setSourceElementId(String sourceElementId);
  public void setSourceElementName(String sourceElementName);
  public void setTargetArtifact(RepositoryArtifact targetArtifact);
  public void setTargetElementId(String targetElementId);
  public void setTargetElementName(String targetElementName);
  public void setComment(String comment);
  public void setLinkType(String linkType);

}
