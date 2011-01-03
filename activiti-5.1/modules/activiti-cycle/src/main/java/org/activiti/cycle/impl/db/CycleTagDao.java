package org.activiti.cycle.impl.db;

import java.util.List;

import org.activiti.cycle.impl.CycleTagContentImpl;
import org.activiti.cycle.impl.db.entity.RepositoryNodeCommentEntity;
import org.activiti.cycle.impl.db.entity.RepositoryNodeTagEntity;


public interface CycleTagDao {

  /*
   * TAGS
   */
  public void insertTag(RepositoryNodeTagEntity tag);

  public void deleteTag(String connectorId, String artifactId, String tagName);

  public List<CycleTagContentImpl> getTagsGroupedByName();

  public List<RepositoryNodeTagEntity> getTagsForNode(String connectorId, String artifactId);

  public CycleTagContentImpl getTagContent(String name);

  /*
   * COMMENTS
   */
  public void insertComment(RepositoryNodeCommentEntity comment);

  public void deleteComment(String id);

  public List<RepositoryNodeCommentEntity> getCommentsForNode(String connectorId, String artifactId);

  public List<String> getSimiliarTagNames(String tagNamePattern);

}
