package org.activiti.cycle.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cycle.CycleService;
import org.activiti.cycle.CycleTagContent;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.db.entity.RepositoryNodeTagEntity;

/**
 * 
 * @author ruecker
 */
public class CycleTagContentImpl implements CycleTagContent {

  private String tagName;

  private long usageCount;
  
  private List<RepositoryNodeTagEntity> containedArtifactIds = new ArrayList<RepositoryNodeTagEntity>();
  private List<RepositoryNode> containedNodes = new ArrayList<RepositoryNode>();

  public CycleTagContentImpl() {
  }

  /**
   * TODO: Should work only with name or alias, not both. But therefor improve
   * DB-Query to be able to work correctly
   */
  public CycleTagContentImpl(String tagName) {
    this.tagName = tagName;
  }
  
  public void addArtifact(RepositoryNodeTagEntity tagEntity) {
    containedArtifactIds.add(tagEntity);
    usageCount = containedArtifactIds.size();
  }
  
  /**
   * resolve the correct {@link RepositoryArtifact}s from the id's loaded from
   * the database
   */
  public void resolveRepositoryArtifacts(CycleService service) {
    for (RepositoryNodeTagEntity tag : containedArtifactIds) {
      // TODO: FUCK, now we need a getRepositoryNode which is hard for Signavio.
      // But this implementation obviously sucks. Improve!
      RepositoryNode node = null;
      try {
        node = service.getRepositoryFolder(tag.getConnectorId(), tag.getNodeId());
      } catch (RepositoryNodeNotFoundException ex) {
        node = service.getRepositoryArtifact(tag.getConnectorId(), tag.getNodeId());
      }

      if (tag.hasAlias()) {
        node.getMetadata().setName(tag.getAlias());
      }
      containedNodes.add(node);
    }
  }

  public long getUsageCount() {
    return usageCount;
  }

  public String getName() {
    return tagName;
  }

  public List<RepositoryNode> getTaggedRepositoryNodes() {
    return containedNodes;
  }
  
  public void setUsageCount(long usageCount) {
    this.usageCount = usageCount;
  }

}
