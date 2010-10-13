package org.activiti.cycle;

import java.io.Serializable;

import org.activiti.engine.impl.db.PersistentObject;


/**
 * One Artifact/Element can have more revisions (versions)
 * 
 * The ArtifactRevision is a persistent entity saved in the Cycle DB.
 * 
 * @author kristin.polenz@camunda.com
 */
public class ArtifactRevision implements Serializable, PersistentObject{
  
  private static final long serialVersionUID = -4359987344641827978L;

  /**
   *  auto generated key, primary key
   **/
  private String id;
  
  private Artifact artifact;

  private Long revision;

  /**
   * default constructor
   */
  public ArtifactRevision() {
  }
  
  
  public String getId() {
    return id;
  }

  
  public void setId(String id) {
    this.id = id;
  }

  
  public Artifact getArtifact() {
    return artifact;
  }

  
  public void setArtifact(Artifact artifact) {
    this.artifact = artifact;
  }

  
  public Long getRevision() {
    return revision;
  }

  
  public void setRevision(Long revision) {
    this.revision = revision;
  }


  public Object getPersistentState() {
    return null;
  }

}
