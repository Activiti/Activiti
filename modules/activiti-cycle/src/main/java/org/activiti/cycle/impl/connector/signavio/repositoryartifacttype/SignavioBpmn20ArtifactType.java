package org.activiti.cycle.impl.connector.signavio.repositoryartifacttype;

import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.artifacttype.AbstractBPMN20ProcessModel;
import org.activiti.cycle.impl.mimetype.XmlMimeType;

/**
 * {@link RepositoryArtifactType} for representing Signavio (or Oryx/Activiti
 * Modeler) BPMN 2.0 Models
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context=CycleContextType.APPLICATION)
public class SignavioBpmn20ArtifactType extends AbstractBPMN20ProcessModel {

  public static final String SIGNAVIO_NAMESPACE_FOR_BPMN_2_0 = "http://b3mn.org/stencilset/bpmn2.0#";
  public static final String ORYX_TYPE_ATTRIBUTE_FOR_BPMN_20 = "BPMN 2.0";

  public MimeType getMimeType() {
    // TODO: which mimetype is this?
    return CycleApplicationContext.get(XmlMimeType.class);
  }

  public String[] getCommonFileExtensions() {
    // this is not a filesystem-based connector, so no filenames and extensions
    // are supported.
    // TODO: is this true?
    return new String[0];
  }

}
