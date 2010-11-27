package org.activiti.cycle.impl.db.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.activiti.cycle.impl.CycleTagContentImpl;
import org.activiti.cycle.impl.db.CycleDAO;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
import org.activiti.cycle.impl.db.entity.RepositoryNodeCommentEntity;
import org.activiti.cycle.impl.db.entity.RepositoryNodePeopleLinkEntity;
import org.activiti.cycle.impl.db.entity.RepositoryNodeTagEntity;
import org.apache.ibatis.session.SqlSession;

public class CycleDaoMyBatisImpl extends AbstractCycleDaoMyBatisImpl implements CycleDAO {

  /**
   * LINKS
   */
  
  @SuppressWarnings("unchecked")
  public List<RepositoryArtifactLinkEntity> getOutgoingArtifactLinks(String sourceConnectorId, String sourceArtifactId) {
    SqlSession session = openSession();
    try {
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("connectorId", sourceConnectorId);
      parameters.put("artifactId", sourceArtifactId);
      List<RepositoryArtifactLinkEntity> linkResultList = session.selectList("selectArtifactLinkForSourceArtifact", parameters);
      if (linkResultList != null) {
        return linkResultList;
      }
      return new ArrayList<RepositoryArtifactLinkEntity>();
    } finally {
      session.close();
    }
  }

  @SuppressWarnings("unchecked")
  public List<RepositoryArtifactLinkEntity> getIncomingArtifactLinks(String targetConnectorId, String targetArtifactId) {
    SqlSession session = openSession();
    try {
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("connectorId", targetConnectorId);
      parameters.put("artifactId", targetArtifactId);
      return session.selectList("selectArtifactLinkForTargetArtifact", parameters);
    } finally {
      session.close();
    }
  }
  
  public void insertArtifactLink(RepositoryArtifactLinkEntity cycleLink) {
    cycleLink.setId(UUID.randomUUID().toString());
    SqlSession session = openSession();
    try {
      session.insert("insertCycleLink", cycleLink);
      session.commit();
    } finally {
      session.close();
    }
  }
  
  public void deleteArtifactLink(String id) {
    SqlSession session = openSession();
    try {
      session.delete("deleteArtifactLink", id);
      session.commit();
    } finally {
      session.close();
    }
  }
  
  /**
   * People-Links
   */
  
  public void insertPeopleLink(RepositoryNodePeopleLinkEntity link) {
    link.setId(UUID.randomUUID().toString());
    SqlSession session = openSession();
    try {
      session.insert("insertPeopleLink", link);
      session.commit();
    } finally {
      session.close();
    }
  }
  
  public void deletePeopleLink(String id) {
    SqlSession session = openSession();
    try {
      session.delete("deletePeopleLink", id);
      session.commit();
    } finally {
      session.close();
    }
  }  

  @SuppressWarnings("unchecked")
  public List<RepositoryNodePeopleLinkEntity> getPeopleLinks(String connectorId, String artifactId) {
    SqlSession session = openSession();
    try {
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("connectorId", connectorId);
      parameters.put("artifactId", artifactId);
      return session.selectList("selectPeopleLinkForSourceArtifact", parameters);
    } finally {
      session.close();
    }
  }

  /**
   * TAGS
   */
  
  public void insertTag(RepositoryNodeTagEntity tag) {
    tag.createId();
    SqlSession session = openSession();
    try {
      session.insert("insertCycleArtifactTag", tag);
      session.commit();
    } finally {
      session.close();
    }    
  }
  
  public void deleteTag(String connectorId, String artifactId, String tagName) {
    SqlSession session = openSession();
    try {
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("connectorId", connectorId);
      parameters.put("artifactId", artifactId);
      parameters.put("name", tagName);
      
      session.delete("deleteArtifactTag", parameters);
      session.commit();
    } finally {
      session.close();
    }
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
  public List<RepositoryNodeTagEntity> getTagsForNode(String connectorId, String artifactId) {
    SqlSession session = openSession();
    try {
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("connectorId", connectorId);
      parameters.put("artifactId", artifactId);

      List<RepositoryNodeTagEntity> linkResultList = session.selectList("selectTagsForNode", parameters);
      if (linkResultList != null) {
        return linkResultList;
      }
      return new ArrayList<RepositoryNodeTagEntity>();
    } finally {
      session.close();
    }
  }

  @SuppressWarnings("unchecked")
  public List<String> getSimiliarTagNames(String tagNamePattern) {
    SqlSession session = openSession();
    try {
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      // TODO: Check correct usage of LIKE
      parameters.put("tagNamePattern", "%" + tagNamePattern + "%");

      // TODO: change to return list of unique strings directly
      List<RepositoryNodeTagEntity> tags = session.selectList("selectSimiliarTagNames", parameters);
      TreeSet<String> result = new TreeSet<String>();
      for (RepositoryNodeTagEntity tagEntity : tags) {
        result.add(tagEntity.getName());
      }
      return new ArrayList<String>(result);
    } finally {
      session.close();
    }
  }

  @SuppressWarnings("unchecked")
  public CycleTagContentImpl getTagContent(String name) {
    SqlSession session = openSession();
    try {
      CycleTagContentImpl tagContent = new CycleTagContentImpl(name);

      List<RepositoryNodeTagEntity> tags = session.selectList("selectTagsByName", name);      
      for (RepositoryNodeTagEntity tag : tags) {
          tagContent.addArtifact(tag);
      }
      return tagContent;
    } finally {
      session.close();
    }
  }

  /**
   * COMMENTS
   */

  public void insertComment(RepositoryNodeCommentEntity comment) {
    SqlSession session = openSession();
    try {
      session.insert("insertCycleComment", comment);
      session.commit();
    } finally {
      session.close();
    }
  }

  public void deleteComment(String id) {
    SqlSession session = openSession();
    try {
      session.delete("deleteCycleCommentById", id);
      session.commit();
    } finally {
      session.close();
    }
  }
  
  @SuppressWarnings("unchecked")
  public List<RepositoryNodeCommentEntity> getCommentsForNode(String connectorId, String artifactId) {
    SqlSession session = openSession();
    try {
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("connectorId", connectorId);
      parameters.put("artifactId", artifactId);

      return session.selectList("selectCycleCommentForSourceArtifact", parameters);
    } finally {
      session.close();
    }
  }

}
