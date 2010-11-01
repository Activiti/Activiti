package org.activiti.cycle.impl.db;

import java.util.List;

import org.activiti.cycle.impl.CycleTagContentImpl;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
import org.activiti.cycle.impl.db.entity.RepositoryNodeCommentEntity;
import org.activiti.cycle.impl.db.entity.RepositoryNodePeopleLinkEntity;
import org.activiti.cycle.impl.db.entity.RepositoryNodeTagEntity;

/**
 * Central DAO to do all relevant DB operations for Cycle entities.
 * 
 * @author ruecker
 */
public interface CycleDAO {

  /**
   * LINKS
   */
  public List<RepositoryArtifactLinkEntity> getOutgoingArtifactLinks(String sourceConnectorId, String sourceArtifactId);

  public List<RepositoryArtifactLinkEntity> getIncomingArtifactLinks(String targetConnectorId, String targetArtifactId);

  public void insertArtifactLink(RepositoryArtifactLinkEntity cycleLink);

  public void deleteArtifactLink(String id);

  /**
   * People-Links
   */
  public void insertPeopleLink(RepositoryNodePeopleLinkEntity link);

  public void deletePeopleLink(String id);

  public List<RepositoryNodePeopleLinkEntity> getPeopleLinks(String connectorId, String artifactId);

  /**
   * TAGS
   */
  public void insertTag(RepositoryNodeTagEntity tag);

  public void deleteTag(String connectorId, String artifactId, String tagName);

  public List<CycleTagContentImpl> getTagsGroupedByName();

  public List<RepositoryNodeTagEntity> getTagsForNode(String connectorId, String artifactId);

  public CycleTagContentImpl getTagContent(String name);

  /**
   * COMMENTS
   */
  public void insertComment(RepositoryNodeCommentEntity comment);

  public void deleteComment(String id);

  public List<RepositoryNodeCommentEntity> getCommentsForNode(String connectorId, String artifactId);


}
