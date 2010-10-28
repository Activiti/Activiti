package org.activiti.cycle;

import java.util.List;

/**
 * Returns a tag with the tagged contents
 * 
 * @author ruecker
 */
public interface CycleTagContent {

  public String getName();
  
  /**
   * return a count how often this tag is used. May be used to visualize tag
   * cloud or the like
   */
  public long getUsageCount();

  public List<RepositoryNode> getTaggedRepositoryNodes();

}
