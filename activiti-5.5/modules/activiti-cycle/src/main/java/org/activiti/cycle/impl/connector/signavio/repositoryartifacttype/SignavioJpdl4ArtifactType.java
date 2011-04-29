package org.activiti.cycle.impl.connector.signavio.repositoryartifacttype;

import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.impl.artifacttype.AbstractProcessModel;
import org.activiti.cycle.impl.mimetype.UnknownMimeType;

/**
 * {@link RepositoryArtifactType} for representing Signavio jBPM 4 models
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent
public class SignavioJpdl4ArtifactType extends AbstractProcessModel {

  public static final String SIGNAVIO_NAMESPACE_FOR_BPMN_JBPM4 = "http://b3mn.org/stencilset/jbpm4#";

  public String getName() {
    return "Signavio jPDL 4";
  }

  public MimeType getMimeType() {
    // TODO: which mimetype to use?
    return new UnknownMimeType();
  }

  public String[] getCommonFileExtensions() {
    return null;
  }

}
