package org.activiti.cycle.impl.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.activiti.cycle.RepositoryNodeComment;
import org.activiti.cycle.impl.db.CycleTagDao;
import org.activiti.cycle.impl.db.entity.RepositoryNodeCommentEntity;
import org.activiti.cycle.service.CycleCommentService;

/**
 * 
 *
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class CycleCommentServiceImpl implements CycleCommentService {

  private CycleTagDao tagDao;

  private CycleServiceConfiguration cycleServiceConfiguration;

  public CycleCommentServiceImpl() {
  }

  public void setCycleServiceConfiguration(CycleServiceConfiguration cycleServiceConfiguration) {
    this.cycleServiceConfiguration = cycleServiceConfiguration;
  }

  public void setTagDao(CycleTagDao tagDao) {
    this.tagDao = tagDao;
  }

  /**
   * perform initialization after dependencies are set.
   */
  public void initialize() {
    // perform initialization
  }

  public void deleteComment(String id) {
    // TODO: the comment related methods should be moved to a dedicated
    // commentDao
    this.tagDao.deleteComment(id);
  }

  public List<RepositoryNodeComment> getCommentsForNode(String connectorId, String artifactId) {
    // TODO: the comment related methods should be moved to a dedicated
    // commentDao
    List<RepositoryNodeComment> comments = new ArrayList<RepositoryNodeComment>();
    for (RepositoryNodeComment comment : this.tagDao.getCommentsForNode(connectorId, artifactId)) {
      comments.add(comment);
    }
    return comments;
  }

  public void insertComment(String connectorId, String nodeId, String elementId, String content, String author, Date date, String answeredCommentId) {
    // TODO: the comment related methods should be moved to a dedicated
    // commentDao
    RepositoryNodeCommentEntity comment = new RepositoryNodeCommentEntity();
    
    comment.setId(UUID.randomUUID().toString());
    comment.setConnectorId(connectorId);
    comment.setNodeId(nodeId);
    comment.setElementId(elementId);
    comment.setContent(content);
    comment.setAuthor(author);
    comment.setDate(date);
    comment.setAnsweredCommentId(answeredCommentId);
    
    this.tagDao.insertComment(comment);
  }

}
