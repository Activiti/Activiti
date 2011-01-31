package org.activiti.cycle.impl.db.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.RepositoryConnectorConfiguration;
import org.activiti.cycle.impl.CycleTagContentImpl;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfigurationImpl;
import org.activiti.cycle.impl.db.CycleCommentDao;
import org.activiti.cycle.impl.db.CycleConfigurationDao;
import org.activiti.cycle.impl.db.CycleRepositoryConnectorConfigurationDao;
import org.activiti.cycle.impl.db.CycleLinkDao;
import org.activiti.cycle.impl.db.CyclePeopleLinkDao;
import org.activiti.cycle.impl.db.CycleTagDao;
import org.activiti.cycle.impl.db.entity.CycleConfigEntity;
import org.activiti.cycle.impl.db.entity.CycleRepositoryConnectorConfigurationEntity;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
import org.activiti.cycle.impl.db.entity.RepositoryNodeCommentEntity;
import org.activiti.cycle.impl.db.entity.RepositoryNodePeopleLinkEntity;
import org.activiti.cycle.impl.db.entity.RepositoryNodeTagEntity;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.Group;
import org.apache.ibatis.session.SqlSession;

public class CycleDaoMyBatisImpl extends AbstractCycleDaoMyBatisImpl implements CycleCommentDao, CycleRepositoryConnectorConfigurationDao, CycleLinkDao,
        CyclePeopleLinkDao, CycleTagDao, CycleConfigurationDao {

  private static Logger log = Logger.getLogger(CycleDaoMyBatisImpl.class.getName());

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
  public List<RepositoryNodeCommentEntity> getCommentsForNode(String connectorId, String nodeId) {
    SqlSession session = openSession();
    try {
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("connectorId", connectorId);
      parameters.put("nodeId", nodeId);

      return session.selectList("selectCycleCommentForSourceArtifact", parameters);
    } finally {
      session.close();
    }
  }

  public List<RepositoryConnectorConfiguration> getRepositoryConnectorConfigurationsForUser(String user) {
    List<RepositoryConnectorConfiguration> resultList = new ArrayList<RepositoryConnectorConfiguration>();
    List<Group> groups = ProcessEngines.getDefaultProcessEngine().getIdentityService().createGroupQuery().groupMember(user).list();
    List<CycleRepositoryConnectorConfigurationEntity> entityList = new ArrayList<CycleRepositoryConnectorConfigurationEntity>();
    // select entities for the groups, this user is a member of
    for (Group group : groups) {
      entityList.addAll(selectRepositoryConnectorConfigurationByGroup(group.getId()));
    }
    // select entities for this user
    entityList.addAll(selectRepositoryConnectorConfigurationByUser(user));
    // convert entities into configurations
    for (CycleRepositoryConnectorConfigurationEntity cycleRepositoryConnectorConfigurationEntity : entityList) {
      resultList.add(cycleRepositoryConnectorConfigurationEntity.asRepositoryConnectorConfiguration());
    }
    return resultList;
  }

  public void saveConfiguration(RepositoryConnectorConfiguration configuration) {
    SqlSession session = openSession();
    try {
      CycleRepositoryConnectorConfigurationEntity entity = CycleRepositoryConnectorConfigurationEntity
              .fromRepositoryConnectorConfiguration((RepositoryConnectorConfigurationImpl) configuration);
      if (null != entity.getId()) {
        session.update("updateCycleRepositoryConnectorConfigurationById", entity);
      } else {
        entity.setId(UUID.randomUUID().toString());
        session.insert("insertRepositoryConnectorConfiguration", entity);
      }
      session.commit();
    } catch (Exception e) {
      log.log(Level.WARNING, "Could not update " + configuration, e);
      session.rollback();
      throw new RuntimeException(e);
    } finally {
      session.close();
    }
  }

  @SuppressWarnings("unchecked")
  private List<CycleRepositoryConnectorConfigurationEntity> selectRepositoryConnectorConfigurationByUser(String id) {
    SqlSession session = openSession();
    try {
      return (List<CycleRepositoryConnectorConfigurationEntity>) session.selectList("selectRepositoryConnectorConfigurationByUser", id);
    } finally {
      session.close();
    }
  }

  @SuppressWarnings("unchecked")
  private List<CycleRepositoryConnectorConfigurationEntity> selectRepositoryConnectorConfiguration(RepositoryConnectorConfiguration configuration) {
    SqlSession session = openSession();
    try {
      return (List<CycleRepositoryConnectorConfigurationEntity>) session.selectList("selectRepositoryConnectorConfigurationByUser", configuration);
    } finally {
      session.close();
    }
  }

  @SuppressWarnings("unchecked")
  private List<CycleRepositoryConnectorConfigurationEntity> selectRepositoryConnectorConfigurationByGroup(String id) {
    SqlSession session = openSession();
    try {
      return (List<CycleRepositoryConnectorConfigurationEntity>) session.selectList("selectRepositoryConnectorConfigurationByGroup", id);
    } finally {
      session.close();
    }
  }

  @SuppressWarnings("unchecked")
  public List<CycleConfigEntity> selectCycleConfigById(String id) {
    SqlSession session = openSession();
    try {
      return (List<CycleConfigEntity>) session.selectList("selectCycleConfigById", id);
    } finally {
      session.close();
    }
  }

  @SuppressWarnings("unchecked")
  public List<CycleConfigEntity> selectCycleConfigByGroup(String group) {
    SqlSession session = openSession();
    try {
      return (List<CycleConfigEntity>) session.selectList("selectCycleConfigByGroup", group);
    } finally {
      session.close();
    }
  }

  public CycleConfigEntity selectCycleConfigByGroupAndKey(String group, String key) {
    SqlSession session = openSession();
    try {
      CycleConfigEntity exampleEntity = new CycleConfigEntity();
      exampleEntity.setGroupName(group);
      exampleEntity.setKey(key);
      return (CycleConfigEntity) session.selectOne("selectCycleConfigByGroupAndKey", exampleEntity);
    } finally {
      session.close();
    }
  }

  @SuppressWarnings("unchecked")
  public List<String> selectCycleConfigurationGroups() {
    SqlSession session = openSession();
    try {
      return (List<String>) session.selectList("selectCycleConfigGroups");
    } finally {
      session.close();
    }
  }

  public void saveCycleConfig(CycleConfigEntity entity) {
    SqlSession session = openSession();
    try {
      if (null != entity.getId()) {
        session.update("updateCycleConfigById", entity);
      } else {
        CycleConfigEntity existingEntity = selectCycleConfigByGroupAndKey(entity.getGroupName(), entity.getKey());
        if (existingEntity != null) {
          throw new RuntimeException("An entity with key '" + entity.getKey() + "' and group '" + entity.getGroupName() + "' already exists. ");
        }
        entity.setId(UUID.randomUUID().toString());
        session.insert("insertCycleConfig", entity);
      }
      session.commit();
    } catch (Exception e) {
      log.log(Level.WARNING, "Could not update " + entity, e);
      session.rollback();
      throw new RuntimeException(e);
    } finally {
      session.close();
    }
  }

}
