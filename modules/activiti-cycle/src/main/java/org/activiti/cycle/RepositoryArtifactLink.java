package org.activiti.cycle;

/**
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

  
  public void setId(String id);
  public void setSourceArtifact(RepositoryArtifact sourceArtifact);
  public void setSourceElementId(String sourceElementId);
  public void setSourceElementName(String sourceElementName);
  public void setTargetArtifact(RepositoryArtifact targetArtifact);
  public void setTargetElementId(String targetElementId);
  public void setTargetElementName(String targetElementName);

}
