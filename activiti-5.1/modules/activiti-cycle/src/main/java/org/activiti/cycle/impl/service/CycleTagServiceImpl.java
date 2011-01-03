package org.activiti.cycle.impl.service;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;

import org.activiti.cycle.CycleTagContent;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryNodeTag;
import org.activiti.cycle.impl.CycleTagContentImpl;
import org.activiti.cycle.impl.connector.view.TagConnectorConfiguration;
import org.activiti.cycle.impl.db.CycleTagDao;
import org.activiti.cycle.impl.db.entity.RepositoryNodeTagEntity;
import org.activiti.cycle.service.CycleTagService;

/**
 * @author bernd.ruecker@camunda.com
 * @author Nils Preusker (nils.preusker@camunda.com)
 * */
public class CycleTagServiceImpl implements CycleTagService {

  private CycleTagDao tagDao;

  private CycleServiceConfiguration cycleServiceConfiguration;

  public CycleTagServiceImpl() {
  }

  public void setCycleServiceConfiguration(CycleServiceConfiguration cycleServiceConfiguration) {
    this.cycleServiceConfiguration = cycleServiceConfiguration;
  }

  public void setTagDao(CycleTagDao tagDao) {
    this.tagDao = tagDao;
  }

  /**
   * perform initialization after dependencies are set.
   */
  public void initialize() {
    // perform initialization
  }

  public void addTag(String connectorId, String artifactId, String tagName, String alias) {
    checkValidConnector(connectorId);

    RepositoryNodeTagEntity tagEntity = new RepositoryNodeTagEntity(tagName, connectorId, artifactId);
    tagEntity.setAlias(alias);
    tagDao.insertTag(tagEntity);
  }

  public void setTags(String connectorId, String nodeId, List<String> tags) {
    checkValidConnector(connectorId);

    // TODO: Improve method to really just update changes!
    List<RepositoryNodeTagEntity> tagsForNode = tagDao.getTagsForNode(connectorId, nodeId);
    for (RepositoryNodeTagEntity tagEntity : tagsForNode) {
      tagDao.deleteTag(connectorId, nodeId, tagEntity.getName());
    }

    HashSet<String> alreadyAddedTags = new HashSet<String>();
    for (String tag : tags) {
      // TODO: Add Alias to setTags method as soon as we have a UI concept for
      // it
      if (tag != null) {
        tag = tag.trim();
      }
      if (!alreadyAddedTags.contains(tag) && tag.length() > 0) {
        addTag(connectorId, nodeId, tag, null);
        alreadyAddedTags.add(tag);
      }
    }
  }

  public List<RepositoryNodeTag> getRepositoryNodeTags(String connectorId, String nodeId) {
    ArrayList<RepositoryNodeTag> list = new ArrayList<RepositoryNodeTag>();
    list.addAll(tagDao.getTagsForNode(connectorId, nodeId));
    return list;
  }

  public List<String> getTags(String connectorId, String nodeId) {
    ArrayList<String> result = new ArrayList<String>();
    List<RepositoryNodeTagEntity> tagsForNode = tagDao.getTagsForNode(connectorId, nodeId);
    for (RepositoryNodeTagEntity tagEntity : tagsForNode) {
      result.add(tagEntity.getName());
    }
    return result;
  }

  public List<String> getSimiliarTagNames(String tagNamePattern) {
    return tagDao.getSimiliarTagNames(tagNamePattern);
  }

  public void deleteTag(String connectorId, String artifactId, String tagName) {
    tagDao.deleteTag(connectorId, artifactId, tagName);
  }

  public List<CycleTagContent> getRootTags() {
    ArrayList<CycleTagContent> result = new ArrayList<CycleTagContent>();
    result.addAll(tagDao.getTagsGroupedByName());
    return result;
  }

  public CycleTagContent getTagContent(String name) {
    CycleTagContentImpl tagContent = tagDao.getTagContent(name);
    tagContent.resolveRepositoryArtifacts();
    return tagContent;
  }

  private void checkValidConnector(String connectorId) {
    if (TagConnectorConfiguration.TAG_CONNECTOR_ID.equals(connectorId)) {
      // we do not want to have recursions in tags
      throw new RepositoryException("Cannot tag a tag");
    }
  }

}
