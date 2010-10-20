package org.activiti.cycle.impl.db;

import java.util.List;

import org.activiti.cycle.impl.db.entity.CycleLink;

public interface CycleLinkService {

  public List<CycleLink> getCycleLinks(String sourceArtifactId);

  public CycleLink findCycleLinkById(String id);

  public void updateCycleLink(CycleLink cycleLink);
}
