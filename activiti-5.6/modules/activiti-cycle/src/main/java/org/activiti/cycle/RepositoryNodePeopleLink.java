package org.activiti.cycle;

public interface RepositoryNodePeopleLink {

  public String getId();
  public String getSourceConnectorId();
  public String getSourceArtifactId();
  public RepositoryArtifact getSourceRepositoryArtifact();
  public Long getSourceRevision();
  public String getUserId();
  public String getGroupId();
  public String getLinkType();
  public String getComment();

}
