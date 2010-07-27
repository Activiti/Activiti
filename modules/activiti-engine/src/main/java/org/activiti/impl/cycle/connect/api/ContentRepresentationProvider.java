package org.activiti.impl.cycle.connect.api;

public abstract class ContentRepresentationProvider {

  private String name;
  
  public ContentRepresentationProvider(String name) {
    this.name = name;
  }

  /**
   * key for name in properties for GUI
   * 
   * TODO: I18n / where? Maybe abstract superclass?
   */
  public String getName() {
    return name;
  }

  public abstract ContentRepresentation createContentRepresentation(RepositoryArtifact artifact, boolean includeBinaryContent);
    
}
