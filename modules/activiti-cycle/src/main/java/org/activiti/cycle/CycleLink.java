package org.activiti.cycle;

/**
 * Link between to {@link RepositoryArtifact}s (maybe that get extended to
 * {@link RepositoryNode}s in the future, but which is not needed at the moment.
 * 
 * The CycleLink is a persistent entity saved in the Cycle DB.
 * 
 * @author ruecker
 */
public class CycleLink {
  
  /**
   * TODO: Add own mini repository for types incling names for forward and
   * reverse direction (like "is implemented by" in this case)
   */
  public static String TYPE_IMPLEMENTS = "implements";
  public static String TYPE_REFINES = "refines";
  public static String TYPE_UNSPECIFIED = "unspecified link";
  
  /**
   * artificial id used as primary key to identify this link
   */
  private long id;
  
  private String sourceArtifactId;
  /**
   * machine readable id of element (what that exactly is depends on the
   * connector, could be the Signavio UUID for example)
   */
  private String sourceElementId;
  /**
   * human readable name of element (what that exactly is depends on the
   * connector, could be the Signavio Task name for example)
   */
  private String sourceElementName;
  private Long sourceRevision;
  
  private String tagetArtifactId;
  private String targetElementId;
  private String targetElementName;
  private Long targetRevision;

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
  
  public String getSourceArtifactId() {
    return sourceArtifactId;
  }

  public void setSourceArtifactId(String sourceArtifactId) {
    this.sourceArtifactId = sourceArtifactId;
  }

  public Long getSourceRevision() {
    return sourceRevision;
  }

  public void setSourceRevision(Long sourceRevision) {
    this.sourceRevision = sourceRevision;
  }

  public String getTargetArtifactId() {
    return tagetArtifactId;
  }

  public void setTargetArtifactId(String targetArtifactId) {
    this.tagetArtifactId = targetArtifactId;
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

  
  public String getSourceElementId() {
    return sourceElementId;
  }

  public void setSourceElementId(String sourceElementId) {
    this.sourceElementId = sourceElementId;
  }

  public String getTargetElementId() {
    return targetElementId;
  }

  public void setTargetElementId(String sourceElementId) {
    this.targetElementId = sourceElementId;
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

  
  public String getSourceElementName() {
    return sourceElementName;
  }

  public void setSourceElementName(String sourceElementName) {
    this.sourceElementName = sourceElementName;
  }

  public String getTargetElementName() {
    return targetElementName;
  }

  public void setTargetElementName(String targetElementName) {
    this.targetElementName = targetElementName;
  }

  
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  
  public boolean isLinkedBothWays() {
    return linkedBothWays;
  }

  public void setLinkedBothWays(boolean linkedBothWays) {
    this.linkedBothWays = linkedBothWays;
  }
}
