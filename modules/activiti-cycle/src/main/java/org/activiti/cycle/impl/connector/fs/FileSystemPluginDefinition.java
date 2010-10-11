package org.activiti.cycle.impl.connector.fs;

import java.util.List;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.ContentType;
import org.activiti.cycle.impl.ArtifactTypeImpl;
import org.activiti.cycle.impl.ContentRepresentationImpl;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.cycle.impl.connector.fs.provider.FileBinaryContentProvider;
import org.activiti.cycle.impl.connector.fs.provider.TextFileContentProvider;
import org.activiti.cycle.impl.connector.fs.provider.XmlFileContentProvider;
import org.activiti.cycle.impl.plugin.ActivitiCyclePlugin;
import org.activiti.cycle.impl.plugin.ActivitiCyclePluginDefinition;

@ActivitiCyclePlugin
public class FileSystemPluginDefinition implements ActivitiCyclePluginDefinition {
  
  public static final String ARTIFACT_TYPE_BPMN_20_XML = "bpmn20.xml";
  public static final String ARTIFACT_TYPE_ORYX_XML = "signavio.xml";
  public static final String ARTIFACT_TYPE_TEXT = "txt";
  public static final String ARTIFACT_TYPE_XML = "xml";
  public static final String ARTIFACT_TYPE_MS_WORD = "doc";
  public static final String ARTIFACT_TYPE_MS_PP = "ppt";
  public static final String ARTIFACT_TYPE_PDF = "pdf";

  public static final String ARTIFACT_TYPE_DEFAULT = "default";

  public static final String CONTENT_REPRESENTATION_ID_XML = "XML";
  public static final String CONTENT_REPRESENTATION_ID_TEXT = "Text";
  public static final String CONTENT_REPRESENTATION_ID_BINARY = "Binary";

  // public static final String CONTENT_REPRESENTATION_ID_MS_WORD_X = "docx";
  // public static final String CONTENT_REPRESENTATION_ID_MS_PP = "ppt";
  // public static final String CONTENT_REPRESENTATION_ID_MS_PP_X = "pptx";
  // public static final String CONTENT_REPRESENTATION_ID_PDF = "pdf";
  
  public void addArtifactTypes(List<ArtifactType> types) {    
    ArtifactTypeImpl artifactTypeDefault = new ArtifactTypeImpl(ARTIFACT_TYPE_DEFAULT, ContentType.TEXT);
    artifactTypeDefault.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_BINARY, ContentType.BINARY),
            new FileBinaryContentProvider());
    artifactTypeDefault.addDownloadContentAction(CONTENT_REPRESENTATION_ID_BINARY);
    types.add(artifactTypeDefault);    
    
    ArtifactTypeImpl artifactType1 = new ArtifactTypeImpl(ARTIFACT_TYPE_BPMN_20_XML, ContentType.XML);
    artifactType1.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_XML, ContentType.XML), new XmlFileContentProvider());
    artifactType1.addDownloadContentAction(CONTENT_REPRESENTATION_ID_XML);
    types.add(artifactType1);

    ArtifactTypeImpl artifactType2 = new ArtifactTypeImpl(ARTIFACT_TYPE_ORYX_XML, ContentType.XML);
    artifactType2.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_XML, ContentType.XML), new XmlFileContentProvider());
    artifactType2.addDownloadContentAction(CONTENT_REPRESENTATION_ID_XML);
    types.add(artifactType2);

    ArtifactTypeImpl artifactType3 = new ArtifactTypeImpl(ARTIFACT_TYPE_TEXT, ContentType.TEXT);
    artifactType3
            .addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_TEXT, ContentType.TEXT),
            new TextFileContentProvider());
    artifactType3.addDownloadContentAction(CONTENT_REPRESENTATION_ID_TEXT);
    types.add(artifactType3);

    ArtifactTypeImpl artifactTypeXml = new ArtifactTypeImpl(ARTIFACT_TYPE_XML, ContentType.XML);
    artifactTypeXml
            .addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_XML, ContentType.XML), new XmlFileContentProvider());
    artifactTypeXml.addDownloadContentAction(CONTENT_REPRESENTATION_ID_XML);
    types.add(artifactTypeXml);

    ArtifactTypeImpl artifactType4 = new ArtifactTypeImpl(ARTIFACT_TYPE_MS_WORD, ContentType.MS_WORD);
    artifactType4.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_BINARY, ContentType.MS_WORD),
            new FileBinaryContentProvider());
    artifactType4.addDownloadContentAction(CONTENT_REPRESENTATION_ID_BINARY);
    types.add(artifactType4);

    ArtifactTypeImpl artifactType5 = new ArtifactTypeImpl(ARTIFACT_TYPE_MS_PP, ContentType.MS_POWERPOINT);
    artifactType5.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_BINARY, ContentType.MS_POWERPOINT),
            new FileBinaryContentProvider());
    artifactType5.addDownloadContentAction(CONTENT_REPRESENTATION_ID_BINARY);
    types.add(artifactType5);

    ArtifactTypeImpl artifactType6 = new ArtifactTypeImpl(ARTIFACT_TYPE_PDF, ContentType.PDF);
    artifactType6.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_BINARY, ContentType.PDF),
            new FileBinaryContentProvider());
    artifactType6.addDownloadContentAction(CONTENT_REPRESENTATION_ID_BINARY);
    types.add(artifactType6);

  }

  public Class< ? extends RepositoryConnectorConfiguration> getRepositoryConnectorConfigurationType() {
    return FileSystemConnectorConfiguration.class;
  }
}
