package org.activiti.cycle.impl.db.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.activiti.cycle.impl.CycleTagContentImpl;
import org.activiti.cycle.impl.db.CycleDAO;
import org.activiti.cycle.impl.db.entity.CycleArtifactTagEntity;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkImpl;
import org.apache.ibatis.session.SqlSession;

public class CycleDaoMyBatisImpl extends AbstractCycleDaoMyBatisImpl implements CycleDAO {

  @SuppressWarnings("unchecked")
  public List<RepositoryArtifactLinkImpl> getOutgoingCycleLinks(String sourceConnectorId, String sourceArtifactId) {
    SqlSession session = openSession();
    try {
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("connectorId", sourceConnectorId);
      parameters.put("artifactId", sourceArtifactId);
      List<RepositoryArtifactLinkImpl> linkResultList = session.selectList("org.activiti.cycle.impl.db.entity.CycleLink.selectArtifactLinkForSourceArtifact", parameters);
      if (linkResultList != null) {
        return linkResultList;
      }
      return new ArrayList<RepositoryArtifactLinkImpl>();
    } finally {
      session.close();
    }
  }

  @SuppressWarnings("unchecked")
  public List<RepositoryArtifactLinkImpl> getIncomingCycleLinks(String targetConnectorId, String targetArtifactId) {
    SqlSession session = openSession();
    try {
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("connectorId", targetConnectorId);
      parameters.put("artifactId", targetArtifactId);
      List<RepositoryArtifactLinkImpl> linkResultList = session.selectList("org.activiti.cycle.impl.db.entity.CycleLink.selectArtifactLinkForTargetArtifact", parameters);
      if (linkResultList != null) {
        return linkResultList;
      }
      return new ArrayList<RepositoryArtifactLinkImpl>();
    } finally {
      session.close();
    }
  }
  
  public void insertCycleLink(RepositoryArtifactLinkImpl cycleLink) {
    SqlSession session = openSession();
    try {
      session.insert("org.activiti.cycle.impl.db.entity.CycleLink.insertCycleLink", cycleLink);
      session.commit();
    } finally {
      session.close();
    }
  }
  
  public void deleteCycleLink(String id) {
    // TODO: IMplement
    throw new RuntimeException("not yet implemented");
  }
  
  public void insertTag(CycleArtifactTagEntity tag) {
    SqlSession session = openSession();
    try {
      session.insert("org.activiti.cycle.impl.db.entity.CycleArtifactTag.insert", tag);
      session.commit();
    } finally {
      session.close();
    }    
  }
  
  public void deleteTag(String connectorId, String artifactId, String tagName) {
    // TODO: IMplement
    throw new RuntimeException("not yet implemented");
  }

  @SuppressWarnings("unchecked")
  public List<CycleTagContentImpl> getTagsGroupedByName() {
    SqlSession session = openSession();
    try {
      List<CycleTagContentImpl> tags = session.selectList("org.activiti.cycle.impl.db.entity.CycleArtifactTag.selectTagsGroupedByName");
      return tags;
    } finally {
      session.close();
    }
  }

  @SuppressWarnings("unchecked")
  public List<CycleArtifactTagEntity> getTagsForNode(String connectorId, String artifactId) {
    SqlSession session = openSession();
    try {
      // TODO: Ad connectorID to query parameter
      List<CycleArtifactTagEntity> linkResultList = session.selectList("org.activiti.cycle.impl.db.entity.CycleArtifactTag.selectTagsForNode", artifactId);
      if (linkResultList != null) {
        return linkResultList;
      }
      return new ArrayList<CycleArtifactTagEntity>();
    } finally {
      session.close();
    }
  }

  @SuppressWarnings("unchecked")
  public CycleTagContentImpl getTagContent(String name) {
    SqlSession session = openSession();
    try {
      CycleTagContentImpl tagContent = new CycleTagContentImpl(name);

      List<CycleArtifactTagEntity> tags = session.selectList("org.activiti.cycle.impl.db.entity.CycleArtifactTag.selectTagsByName", name);      
      for (CycleArtifactTagEntity tag : tags) {
          tagContent.addArtifact(tag);
      }
      return tagContent;
    } finally {
      session.close();
    }
  }

}
