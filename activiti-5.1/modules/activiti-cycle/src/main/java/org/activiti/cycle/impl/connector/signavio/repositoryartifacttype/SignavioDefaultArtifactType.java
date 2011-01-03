package org.activiti.cycle.impl.connector.signavio.repositoryartifacttype;

import org.activiti.cycle.MimeType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.artifacttype.AbstractRepositoryArtifactType;
import org.activiti.cycle.impl.mimetype.JsonMimeType;

/**
 * Artifact type for Signavio Artifact, which are not
 * {@link SignavioBpmn20ArtifactType}s or {@link SignavioJpdl4ArtifactType}s
 * (i.e. Process Landscapes).
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class SignavioDefaultArtifactType extends AbstractRepositoryArtifactType {

  public String getName() {
    return "Signavio Artifact";
  }

  public MimeType getMimeType() {
    return CycleApplicationContext.get(JsonMimeType.class);
  }

  public String[] getCommonFileExtensions() {
    return new String[0];
  }

}
