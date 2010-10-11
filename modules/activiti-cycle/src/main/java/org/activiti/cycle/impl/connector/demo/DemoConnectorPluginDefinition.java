package org.activiti.cycle.impl.connector.demo;

import java.util.List;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.ContentType;
import org.activiti.cycle.impl.ArtifactTypeImpl;
import org.activiti.cycle.impl.ContentRepresentationImpl;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.cycle.impl.connector.demo.action.CopyArtifactAction;
import org.activiti.cycle.impl.connector.demo.action.OpenActivitiAction;
import org.activiti.cycle.impl.connector.demo.provider.DemoProvider;
import org.activiti.cycle.impl.connector.demo.provider.ExceptionProvider;
import org.activiti.cycle.impl.plugin.ActivitiCyclePlugin;
import org.activiti.cycle.impl.plugin.ActivitiCyclePluginDefinition;

@ActivitiCyclePlugin
public class DemoConnectorPluginDefinition implements ActivitiCyclePluginDefinition {
  
  public static final String ARTIFACT_TYPE_TEXT = "ARTIFACT_TYPE_TEXT";
  public static final String ARTIFACT_TYPE_MINDMAP = "ARTIFACT_TYPE_MINDMAP";
  public static final String ARTIFACT_TYPE_BPMN_20 = "ARTIFACT_TYPE_BPMN_20";

  public static final String CONTENT_REPRESENTATION_ID_TEXT = "TEXT";
  public static final String CONTENT_REPRESENTATION_ID_EXCEPTION = "EXCEPTION";
  public static final String CONTENT_REPRESENTATION_ID_PNG = "PNG";
  public static final String CONTENT_REPRESENTATION_ID_XML = "XML";

  public void addArtifactTypes(List<ArtifactType> types) {
    ArtifactTypeImpl artifactType1 = new ArtifactTypeImpl(ARTIFACT_TYPE_TEXT, ContentType.TEXT);
    artifactType1.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_TEXT, ContentType.TEXT), new DemoProvider(
            CONTENT_REPRESENTATION_ID_TEXT));
    artifactType1.addContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_EXCEPTION, ContentType.TEXT), new ExceptionProvider());
    artifactType1.addParameterizedAction(new CopyArtifactAction());
    artifactType1.addOpenUrlAction(new OpenActivitiAction());
    artifactType1.addDownloadContentAction(CONTENT_REPRESENTATION_ID_TEXT);
    types.add(artifactType1);

    ArtifactTypeImpl artifactType2 = new ArtifactTypeImpl(ARTIFACT_TYPE_MINDMAP, ContentType.XML);
    artifactType2.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_TEXT, ContentType.TEXT), new DemoProvider(
            CONTENT_REPRESENTATION_ID_TEXT));
    artifactType2.addContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_PNG, ContentType.PNG), new DemoProvider(
            CONTENT_REPRESENTATION_ID_PNG));
    types.add(artifactType2);

    ArtifactTypeImpl artifactType3 = new ArtifactTypeImpl(ARTIFACT_TYPE_BPMN_20, ContentType.XML);
    artifactType3.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_XML, ContentType.TEXT), new DemoProvider(
            CONTENT_REPRESENTATION_ID_XML));
    artifactType3.addParameterizedAction(new CopyArtifactAction());
    artifactType3.addOpenUrlAction(new OpenActivitiAction());
    artifactType3.addContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_PNG, ContentType.PNG), new DemoProvider(
            CONTENT_REPRESENTATION_ID_PNG));
    types.add(artifactType3);
  }

  public Class< ? extends RepositoryConnectorConfiguration> getRepositoryConnectorConfigurationType() {
    return DemoConnectorConfiguration.class;
  }  
}
