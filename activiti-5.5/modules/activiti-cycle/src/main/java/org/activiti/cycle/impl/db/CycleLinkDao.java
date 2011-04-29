package org.activiti.cycle.impl.db;

import java.util.List;

import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;


public interface CycleLinkDao {

  public List<RepositoryArtifactLinkEntity> getOutgoingArtifactLinks(String sourceConnectorId, String sourceArtifactId);

  public List<RepositoryArtifactLinkEntity> getIncomingArtifactLinks(String targetConnectorId, String targetArtifactId);

  public void insertArtifactLink(RepositoryArtifactLinkEntity cycleLink);

  public void deleteArtifactLink(String id);

}
