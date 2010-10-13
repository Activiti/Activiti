package org.activiti.cycle;

import java.io.Serializable;

import org.activiti.engine.impl.db.PersistentObject;

/**
 * Link between to {@link RepositoryArtifact}s (maybe that get extended to
 * {@link RepositoryNode}s in the future, but which is not needed at the moment.
 * 
 * The CycleLink is a persistent entity saved in the Cycle DB.
 * 
 * @author ruecker, kristin.polenz@camunda.com
 */
public class CycleLink implements Serializable, PersistentObject{
  
  private static final long serialVersionUID = 5975388781338451533L;
  
  /**
   * TODO: Add own mini repository for types incling names for forward and
   * reverse direction (like "is implemented by" in this case)
   */
  public static String TYPE_IMPLEMENTS = "implements";
  public static String TYPE_REFINES = "refines";
  public static String TYPE_UNSPECIFIED = "unspecified link";
  
  /**
   * target artifact id
   */
  private String id;
  private String targetElementId;
  private String targetElementName;
  private Long targetRevision;
  private Artifact sourceArtifact;

  /**
   * default constructor
   */
  public CycleLink() {
  }
  
  public CycleLink(String tagetArtifactId, String targetElementId, String targetElementName, Long targetRevision, Artifact sourceArtifact, String linkType,
          String description, boolean linkedBothWays) {
    this.id = tagetArtifactId;
    this.targetElementId = targetElementId;
    this.targetElementName = targetElementName;
    this.targetRevision = targetRevision;
    this.sourceArtifact = sourceArtifact;
    this.linkType = linkType;
    this.description = description;
    this.linkedBothWays = linkedBothWays;
  }

  /**
   * type of the link as String. Basically you could supply what you want, but
   * often it make sense to just use the supplied constants if possible
   */
  private String linkType;

  /**
   * additional description maybe supplied by the user for this link
   */
  private String description;

  /**
   * indicate if the link is found in both directions, default is true. If false
   * the link is only found when searching links for artifactId1
   */
  private boolean linkedBothWays = true;
  

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getTargetRevision() {
    return targetRevision;
  }

  public void setTargetRevision(Long targetRevision) {
    this.targetRevision = targetRevision;
  }

  public String getLinkType() {
    return linkType;
  }

  public void setLinkType(String linkType) {
    this.linkType = linkType;
  }

  public String getTargetElementId() {
    return targetElementId;
  }

  public void setTargetElementId(String sourceElementId) {
    this.targetElementId = sourceElementId;
  }
  
  public Artifact getSourceArtifact() {
    return sourceArtifact;
  }

  
  public void setSourceArtifact(Artifact sourceArtifact) {
    this.sourceArtifact = sourceArtifact;
  }

  /**
   * gets additional description maybe supplied by the user for this link
   */
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTargetElementName() {
    return targetElementName;
  }

  public void setTargetElementName(String targetElementName) {
    this.targetElementName = targetElementName;
  }
  
  public boolean isLinkedBothWays() {
    return linkedBothWays;
  }

  public void setLinkedBothWays(boolean linkedBothWays) {
    this.linkedBothWays = linkedBothWays;
  }

  public Object getPersistentState() {
    return null;
  }
}
