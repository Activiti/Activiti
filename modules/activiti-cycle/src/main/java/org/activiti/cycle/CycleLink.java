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
  
  private String artifactId1;
  /**
   * machine readable id of element (what that exactly is depends on the
   * connector, could be the Signavio UUID for example)
   */
  private String elementId1;
  /**
   * human readable name of element (what that exactly is depends on the
   * connector, could be the Signavio Task name for example)
   */
  private String elementName1;
  private Long revision1;
  
  private String artifactId2;
  private String elementId2;
  private String elementName2;
  private Long revision2;

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
  
  public String getArtifactId1() {
    return artifactId1;
  }

  public void setArtifactId1(String artifactId1) {
    this.artifactId1 = artifactId1;
  }

  public Long getRevision1() {
    return revision1;
  }

  public void setRevision1(Long revision1) {
    this.revision1 = revision1;
  }

  public String getArtifactId2() {
    return artifactId2;
  }

  public void setArtifactId2(String artifactId2) {
    this.artifactId2 = artifactId2;
  }

  public Long getRevision2() {
    return revision2;
  }

  public void setRevision2(Long revision2) {
    this.revision2 = revision2;
  }

  public String getLinkType() {
    return linkType;
  }

  public void setLinkType(String linkType) {
    this.linkType = linkType;
  }

  
  public String getElementId1() {
    return elementId1;
  }

  public void setElementId1(String elementId1) {
    this.elementId1 = elementId1;
  }

  public String getElementId2() {
    return elementId2;
  }

  public void setElementId2(String elementId2) {
    this.elementId2 = elementId2;
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

  
  public String getElementName1() {
    return elementName1;
  }

  public void setElementName1(String elementName1) {
    this.elementName1 = elementName1;
  }

  public String getElementName2() {
    return elementName2;
  }

  public void setElementName2(String elementName2) {
    this.elementName2 = elementName2;
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
