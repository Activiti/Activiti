package org.activiti.cycle.impl.connector.signavio;

import java.util.List;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.ContentType;
import org.activiti.cycle.impl.ArtifactTypeImpl;
import org.activiti.cycle.impl.ContentRepresentationImpl;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.cycle.impl.connector.signavio.action.CreateTechnicalBpmnXmlAction;
import org.activiti.cycle.impl.connector.signavio.action.OpenModelerAction;
import org.activiti.cycle.impl.connector.signavio.action.ValidateActivitiDeployment;
import org.activiti.cycle.impl.connector.signavio.provider.ActivitiCompliantBpmn20Provider;
import org.activiti.cycle.impl.connector.signavio.provider.Bpmn20Provider;
import org.activiti.cycle.impl.connector.signavio.provider.Jpdl4Provider;
import org.activiti.cycle.impl.connector.signavio.provider.JsonProvider;
import org.activiti.cycle.impl.connector.signavio.provider.MoviProvider;
import org.activiti.cycle.impl.connector.signavio.provider.PngProvider;
import org.activiti.cycle.impl.plugin.ActivitiCyclePlugin;
import org.activiti.cycle.impl.plugin.ActivitiCyclePluginDefinition;

@ActivitiCyclePlugin
public class SignavioPluginDefinition implements ActivitiCyclePluginDefinition {

  // register Signavio stencilsets to identify file types
  public static final String SIGNAVIO_NAMESPACE_FOR_BPMN_2_0 = "http://b3mn.org/stencilset/bpmn2.0#";
  public static final String SIGNAVIO_NAMESPACE_FOR_BPMN_JBPM4 = "http://b3mn.org/stencilset/jbpm4#";

  /**
   * Information for process landscape:
   * 
   * "type": "Prozesslandkarte"
   * 
   * "namespace": "http://www.signavio.com/stencilsets/processmap#"
   */

  public static final String BPMN_2_0_XML = "bpm2.0";
  public static final String ORYX_TYPE_ATTRIBUTE_FOR_BPMN_20 = "BPMN 2.0";

  public static final String ARTIFACT_TYPE_BPMN_20 = "BPMN 2.0";
  public static final String ARTIFACT_TYPE_BPMN_FOR_JPDL4 = "jPDL 4";
  public static final String ARTIFACT_TYPE_DEFAULT = "DEFAULT";
  
  public static final String CONTENT_REPRESENTATION_ID_PNG = "PNG";
  public static final String CONTENT_REPRESENTATION_ID_JSON = "JSON";
  public static final String CONTENT_REPRESENTATION_ID_BPMN_20_DEVELOPER = "Developer Friendly BPMN 2.0";
  public static final String CONTENT_REPRESENTATION_ID_BPMN_20_RAW = "Raw BPMN 2.0";
  public static final String CONTENT_REPRESENTATION_ID_JPDL4 = "jPDL 4";
  public static final String CONTENT_REPRESENTATION_ID_MOVI = "MOVI";
  
  
  
 public void addArtifactTypes(List<ArtifactType> types) {
   // TODO: How can we differentiate between these at least in the naming? The
    // type Oryx and Signavio is almost the same, but not completly
    // list.add(new ArtifactType("Activiti Modeler BPMN 2.0",
    // SignavioConnector.ORYX_TYPE_ATTRIBUTE_FOR_BPMN_20));
    // list.add(new ArtifactType("Signavio BPMN 2.0",
    // SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_2_0));
    // list.add(new ArtifactType("Signavio BPMN for jBPM 4",
    // SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_JBPM4));

   
    ArtifactTypeImpl artifactType1 = new ArtifactTypeImpl(ARTIFACT_TYPE_BPMN_20);
    artifactType1.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_PNG, ContentType.PNG), new PngProvider());
    artifactType1.addContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_BPMN_20_DEVELOPER, ContentType.XML),
            new ActivitiCompliantBpmn20Provider());
    artifactType1.addContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_BPMN_20_RAW, ContentType.XML), new Bpmn20Provider());
    artifactType1.addContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_JSON, ContentType.XML), new JsonProvider());
    artifactType1.addContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_MOVI, ContentType.JAVASCRIPT), new MoviProvider());
    
    artifactType1.addParameterizedAction(new CreateTechnicalBpmnXmlAction());
    artifactType1.addParameterizedAction(new ValidateActivitiDeployment());
    artifactType1.addOpenUrlAction(new OpenModelerAction());
    artifactType1.addDownloadContentAction(CONTENT_REPRESENTATION_ID_BPMN_20_DEVELOPER);
    artifactType1.addDownloadContentAction(CONTENT_REPRESENTATION_ID_BPMN_20_RAW);
    artifactType1.addDownloadContentAction(CONTENT_REPRESENTATION_ID_JSON);
    types.add(artifactType1);
   
   
    ArtifactTypeImpl artifactType2 = new ArtifactTypeImpl(ARTIFACT_TYPE_BPMN_FOR_JPDL4);
    artifactType2.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_PNG, ContentType.PNG), new PngProvider());
    artifactType2.addContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_JPDL4, ContentType.XML), new Jpdl4Provider());
    artifactType2.addContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_JSON, ContentType.XML), new JsonProvider());

    artifactType2.addOpenUrlAction(new OpenModelerAction());
    artifactType2.addDownloadContentAction(CONTENT_REPRESENTATION_ID_JPDL4);
    artifactType2.addDownloadContentAction(CONTENT_REPRESENTATION_ID_JSON);
    types.add(artifactType2);
   
    // TODO: Retrieve model through modellink (without /info) and dynamically
    // initialize RepositoryRegistry with supported formats?

    // TODO: Check if really any artifact in Signavio has a PNG?
    ArtifactTypeImpl artifactTypeDefault = new ArtifactTypeImpl(ARTIFACT_TYPE_DEFAULT);
    artifactTypeDefault.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_PNG, ContentType.PNG), new PngProvider());
    types.add(artifactTypeDefault);
  }

  public Class< ? extends RepositoryConnectorConfiguration> getRepositoryConnectorConfigurationType() {
    return SignavioConnectorConfiguration.class;
  }
  
}
