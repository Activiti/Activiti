package org.activiti.cycle.impl.db;

import java.util.List;

import org.activiti.cycle.impl.db.entity.RepositoryNodePeopleLinkEntity;


public interface CyclePeopleLinkDao {

  public void insertPeopleLink(RepositoryNodePeopleLinkEntity link);

  public void deletePeopleLink(String id);

  public List<RepositoryNodePeopleLinkEntity> getPeopleLinks(String connectorId, String artifactId);

}
