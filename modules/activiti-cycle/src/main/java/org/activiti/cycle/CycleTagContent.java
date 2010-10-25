package org.activiti.cycle;

import java.util.List;

/**
 * Returns a tag with the tagged contents
 * 
 * @author ruecker
 */
public interface CycleTagContent {

  public String getName();

  public List<RepositoryNode> getTaggedRepositoryNodes();

}
