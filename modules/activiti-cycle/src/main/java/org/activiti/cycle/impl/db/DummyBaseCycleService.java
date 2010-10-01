package org.activiti.cycle.impl.db;

import java.util.List;

import org.activiti.cycle.CycleLink;
import org.activiti.cycle.CycleService;
import org.activiti.cycle.CycleTag;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.connector.demo.DemoConnector;

/**
 * dummy implementation for cycle using hard coded data for
 * {@link DemoConnector}. Just a place holder throw away as soon we have a real
 * DB implementation.
 */
public abstract class DummyBaseCycleService implements CycleService {
  
  public void addArtifactLink(String sourceArtifactId, String targetArtifactId) {
  }

  public void addLink(CycleLink link) {
  }

  public void addTag(String nodeId, String tagName) {
  }

  public void addTag(String nodeId, String tagName, String alias) {
  }

  public void deleteLink(long linkId) {
  }

  public void deleteTag(String nodeId, String tagName) {
  }

  public List<CycleTag> getAllTags() {
    return null;
  }

  public List<CycleTag> getAllTagsIgnoreAlias() {
    return null;
  }

  public List<CycleLink> getArtifactLinks(String sourceArtifactId) {
    return null;
  }

  public List<CycleLink> getArtifactLinks(String sourceArtifactId, Long sourceRevision) {
    return null;
  }

  public List<CycleLink> getArtifactLinks(String sourceArtifactId, String type) {
    return null;
  }

  public List<CycleLink> getArtifactLinks(String sourceArtifactId, Long sourceRevision, String type) {
    return null;
  }

  public List<CycleTag> getTags(String nodeId) throws RepositoryNodeNotFoundException {
    return null;
  }

}
