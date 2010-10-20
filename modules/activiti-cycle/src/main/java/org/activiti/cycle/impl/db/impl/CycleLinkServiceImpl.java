package org.activiti.cycle.impl.db.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cycle.impl.db.CycleLinkService;
import org.activiti.cycle.impl.db.entity.CycleLink;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class CycleLinkServiceImpl extends CycleDbService implements CycleLinkService {

  public CycleLink findCycleLinkById(String id) {
    // TODO
    throw new RuntimeException("Not implemented");
  }

  @SuppressWarnings("unchecked")
  public List<CycleLink> getCycleLinks(String sourceArtifactId) {
    SqlSessionFactory sqlMapper = getSessionFactory();
    SqlSession session = sqlMapper.openSession();
    List<CycleLink> linkResultList;
    try {
      linkResultList = session.selectList("org.activiti.cycle.impl.db.entity.CycleLink.selectArtifactLinkForSourceArtifact", sourceArtifactId);
    } finally {
      session.close();
    }
    if (linkResultList != null) {
      return linkResultList;
    }
    return new ArrayList<CycleLink>();
  }

  public void updateCycleLink(CycleLink cycleLink) {
    SqlSessionFactory sqlMapper = getSessionFactory();
    SqlSession session = sqlMapper.openSession();

    session.insert("org.activiti.cycle.impl.db.entity.CycleLink.insertCycleLink", cycleLink);
    session.commit();
  }

}
