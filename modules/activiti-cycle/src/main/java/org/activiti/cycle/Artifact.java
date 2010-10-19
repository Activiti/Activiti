package org.activiti.cycle;

import java.io.Serializable;
import java.util.List;

import org.activiti.engine.impl.db.PersistentObject;

/**
 * Link between to {@link RepositoryArtifact}s (maybe that get extended to
 * {@link RepositoryNode}s in the future, but which is not needed at the moment.
 * 
 * The Artifact is a persistent entity saved in the Cycle DB.
 * 
 * @author ruecker, kristin.polenz@camunda.com
 */
/**
 * @deprecated
 * This entity is not necessary because it is enough to have a simple entity class 
 * and one table for the cycle link persistence.
 */
public class Artifact implements Serializable, PersistentObject {
  
  private static final long serialVersionUID = -7697263644413286865L;
  
  /**
   * old stuff from first draft of CycleLink
   */  
//  /**
//   * TODO: Add own mini repository for types incling names for forward and
//   * reverse direction (like "is implemented by" in this case)
//   */
//  public static String TYPE_IMPLEMENTS = "implements";
//  public static String TYPE_REFINES = "refines";
//  public static String TYPE_UNSPECIFIED = "unspecified link";
  
  /**
   * artifactId
   */
  private String id;
  /**
   * machine readable id of element (what that exactly is depends on the
   * connector, could be the Signavio UUID for example)
   */
  private String elementId;
  /**
   * human readable name of element (what that exactly is depends on the
   * connector, could be the Signavio Task name for example)
   */
  private String elementName;
  
  private List<CycleLink> cycleLinkList;
  
  private List<ArtifactRevision> artifactRevisionList;

  /**
   * old stuff from first draft of CycleLink
   */
  //private Long sourceRevision;

  
  /**
   * default constructor
   */
  public Artifact() {
  }
  
  public Artifact(String id, String elementId, String elementName, List<CycleLink> cycleLinkList, List<ArtifactRevision> artifactRevisionList) {
    this.id = id;
    this.elementId = elementId;
    this.elementName = elementName;
    this.cycleLinkList = cycleLinkList;
    this.artifactRevisionList = artifactRevisionList;
  }


  /**
   * old stuff from first draft of CycleLink
   */
//  private String tagetArtifactId;
//  private String targetElementId;
//  private String targetElementName;
//  private Long targetRevision;
//
//  /**
//   * type of the link as String. Basically you could supply what you want, but
//   * often it make sense to just use the supplied constants if possible
//   */
//  private String linkType;
//
//  /**
//   * additional description maybe supplied by the user for this link
//   */
//  private String description;
//
//  /**
//   * indicate if the link is found in both directions, default is true. If false
//   * the link is only found when searching links for artifactId1
//   */
//  private boolean linkedBothWays = true;
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }
  
  public String getElementName() {
    return elementName;
  }

  public void setElementName(String elementName) {
    this.elementName = elementName;
  }


  public List<CycleLink> getCycleLinkList() {
    return cycleLinkList;
  }
  
  public void setCycleLinkList(List<CycleLink> cycleLinkList) {
    this.cycleLinkList = cycleLinkList;
  }
  
  public List<ArtifactRevision> getArtifactRevisionList() {
    return artifactRevisionList;
  }
  
  public void setArtifactRevisionList(List<ArtifactRevision> artifactRevisionList) {
    this.artifactRevisionList = artifactRevisionList;
  }

  public Object getPersistentState() {
    return null;
  }

  /**
   * old stuff from first draft of CycleLink
   */
//public Long getSourceRevision() {
//return sourceRevision;
//}
//
//public void setSourceRevision(Long sourceRevision) {
//this.sourceRevision = sourceRevision;
//}
//  
//public String getTargetArtifactId() {
//return tagetArtifactId;
//}
//
//public void setTargetArtifactId(String targetArtifactId) {
//this.tagetArtifactId = targetArtifactId;
//}
//
//public Long getTargetRevision() {
//return targetRevision;
//}
//
//public void setTargetRevision(Long targetRevision) {
//this.targetRevision = targetRevision;
//}
//
//public String getLinkType() {
//return linkType;
//}
//
//public void setLinkType(String linkType) {
//this.linkType = linkType;
//}  
  
//public String getTargetElementId() {
//return targetElementId;
//}
//
//public void setTargetElementId(String sourceElementId) {
//this.targetElementId = sourceElementId;
//}
//
///**
//* gets additional description maybe supplied by the user for this link
//*/
//public String getDescription() {
//return description;
//}
//
//public void setDescription(String description) {
//this.description = description;
//}
  
//  public String getTargetElementName() {
//    return targetElementName;
//  }
//
//  public void setTargetElementName(String targetElementName) {
//    this.targetElementName = targetElementName;
//  }
  
//  public boolean isLinkedBothWays() {
//    return linkedBothWays;
//  }
//
//  public void setLinkedBothWays(boolean linkedBothWays) {
//    this.linkedBothWays = linkedBothWays;
//  }
}
