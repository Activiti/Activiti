package org.activiti.cycle;

import java.util.Date;

/**
 * A comment to a {@link RepositoryNode}. Maybe this is a response to another
 * {@link RepositoryNodeComment}, which already allows easy discussions going on
 * got {@link RepositoryNode}s
 * 
 * @author ruecker
 */
public interface RepositoryNodeComment {

  public String getId();

  public void setId(String id);

  public String getConnectorId();

  public void setConnectorId(String connectorId);

  public String getNodeId();

  public void setNodeId(String nodeId);

  public String getElementId();

  public void setElementId(String elementId);

  public String getContent();

  public void setContent(String content);

  public String getAuthor();

  public void setAuthor(String author);

  public Date getDate();

  public void setDate(Date date);

  public String getAnsweredCommentId();

  public void setAnsweredCommentId(String answeredCommentId);

}
