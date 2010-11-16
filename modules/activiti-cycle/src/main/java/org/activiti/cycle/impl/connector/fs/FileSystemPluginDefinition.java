package org.activiti.cycle.impl.connector.fs;

import java.util.List;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.CycleDefaultMimeType;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.impl.ArtifactTypeImpl;
import org.activiti.cycle.impl.ContentRepresentationImpl;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.cycle.impl.connector.fs.provider.FileBinaryContentProvider;
import org.activiti.cycle.impl.connector.fs.provider.TextFileContentProvider;
import org.activiti.cycle.impl.connector.fs.provider.XmlFileContentProvider;
import org.activiti.cycle.impl.connector.signavio.SignavioPluginDefinition;
import org.activiti.cycle.impl.plugin.ActivitiCyclePlugin;
import org.activiti.cycle.impl.plugin.ActivitiCyclePluginDefinition;

@ActivitiCyclePlugin
public class FileSystemPluginDefinition implements ActivitiCyclePluginDefinition {

  public static final String ARTIFACT_TYPE_DEFAULT = "default";
  public static final String ARTIFACT_TYPE_BPMN_20_XML = "bpmn20.xml";
  public static final String ARTIFACT_TYPE_ORYX_XML = "signavio.xml";
  public static final String ARTIFACT_TYPE_TEXT = "txt";
  public static final String ARTIFACT_TYPE_XML = "xml";
  public static final String ARTIFACT_TYPE_MS_WORD = "doc";
  public static final String ARTIFACT_TYPE_MS_PP = "ppt";
  public static final String ARTIFACT_TYPE_PDF = "pdf";
  public static final String ARTIFACT_TYPE_HTML = "html";
  public static final String ARTIFACT_TYPE_JSON = "json";

  public static final String CONTENT_REPRESENTATION_ID_XML = "XML";
  public static final String CONTENT_REPRESENTATION_ID_TEXT = "Text";
  public static final String CONTENT_REPRESENTATION_ID_BINARY = "Binary";
  public static final String CONTENT_REPRESENTATION_ID_HTML = "HTML";
  public static final String CONTENT_REPRESENTATION_ID_HTML_SOURCE = "HTML source";
  public static final String CONTENT_REPRESENTATION_ID_JSON = SignavioPluginDefinition.CONTENT_REPRESENTATION_ID_JSON;

  // public static final String CONTENT_REPRESENTATION_ID_MS_WORD_X = "docx";
  // public static final String CONTENT_REPRESENTATION_ID_MS_PP = "ppt";
  // public static final String CONTENT_REPRESENTATION_ID_MS_PP_X = "pptx";
  // public static final String CONTENT_REPRESENTATION_ID_PDF = "pdf";

  public void addArtifactTypes(List<ArtifactType> types) {
    ArtifactTypeImpl artifactTypeDefault = new ArtifactTypeImpl(ARTIFACT_TYPE_DEFAULT, CycleDefaultMimeType.TEXT);
    artifactTypeDefault.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_TEXT, CycleDefaultMimeType.TEXT,
            RenderInfo.TEXT_PLAIN), new FileBinaryContentProvider());
    artifactTypeDefault.addDownloadContentAction(CONTENT_REPRESENTATION_ID_TEXT);
    types.add(artifactTypeDefault);

    ArtifactTypeImpl artifactType1 = new ArtifactTypeImpl(ARTIFACT_TYPE_BPMN_20_XML, CycleDefaultMimeType.XML);
    artifactType1.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_XML, CycleDefaultMimeType.XML, RenderInfo.CODE),
            new XmlFileContentProvider());
    artifactType1.addDownloadContentAction(CONTENT_REPRESENTATION_ID_XML);
    types.add(artifactType1);

    ArtifactTypeImpl artifactType2 = new ArtifactTypeImpl(ARTIFACT_TYPE_ORYX_XML, CycleDefaultMimeType.XML);
    artifactType2.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_XML, CycleDefaultMimeType.XML, RenderInfo.CODE),
            new XmlFileContentProvider());
    artifactType2.addDownloadContentAction(CONTENT_REPRESENTATION_ID_XML);
    types.add(artifactType2);

    ArtifactTypeImpl artifactType3 = new ArtifactTypeImpl(ARTIFACT_TYPE_TEXT, CycleDefaultMimeType.TEXT);
    artifactType3.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_TEXT, CycleDefaultMimeType.TEXT,
            RenderInfo.TEXT_PLAIN), new TextFileContentProvider());
    artifactType3.addDownloadContentAction(CONTENT_REPRESENTATION_ID_TEXT);
    types.add(artifactType3);

    ArtifactTypeImpl artifactTypeXml = new ArtifactTypeImpl(ARTIFACT_TYPE_XML, CycleDefaultMimeType.XML);
    artifactTypeXml.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_XML, CycleDefaultMimeType.XML, RenderInfo.CODE),
            new XmlFileContentProvider());
    artifactTypeXml.addDownloadContentAction(CONTENT_REPRESENTATION_ID_XML);
    types.add(artifactTypeXml);

    ArtifactTypeImpl artifactType4 = new ArtifactTypeImpl(ARTIFACT_TYPE_MS_WORD, CycleDefaultMimeType.MS_WORD);
    artifactType4.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_BINARY, CycleDefaultMimeType.MS_WORD,
            RenderInfo.BINARY), new FileBinaryContentProvider());
    artifactType4.addDownloadContentAction(CONTENT_REPRESENTATION_ID_BINARY);
    types.add(artifactType4);

    ArtifactTypeImpl artifactType5 = new ArtifactTypeImpl(ARTIFACT_TYPE_MS_PP, CycleDefaultMimeType.MS_POWERPOINT);
    artifactType5.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_BINARY, CycleDefaultMimeType.MS_POWERPOINT,
            RenderInfo.BINARY), new FileBinaryContentProvider());
    artifactType5.addDownloadContentAction(CONTENT_REPRESENTATION_ID_BINARY);
    types.add(artifactType5);

    ArtifactTypeImpl artifactType6 = new ArtifactTypeImpl(ARTIFACT_TYPE_PDF, CycleDefaultMimeType.PDF);
    artifactType6.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_BINARY, CycleDefaultMimeType.PDF, RenderInfo.BINARY),
            new FileBinaryContentProvider());
    artifactType6.addDownloadContentAction(CONTENT_REPRESENTATION_ID_BINARY);
    types.add(artifactType6);

    ArtifactTypeImpl artifactTypeHtml = new ArtifactTypeImpl(ARTIFACT_TYPE_HTML, CycleDefaultMimeType.HTML);
    artifactTypeHtml.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_HTML, CycleDefaultMimeType.HTML, RenderInfo.HTML),
            new TextFileContentProvider());
    artifactTypeHtml.addDownloadContentAction(CONTENT_REPRESENTATION_ID_HTML);
    artifactTypeHtml.addContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_HTML_SOURCE, CycleDefaultMimeType.HTML, RenderInfo.CODE),
            new TextFileContentProvider());
    types.add(artifactTypeHtml);

    ArtifactTypeImpl artifactTypeJson = new ArtifactTypeImpl(ARTIFACT_TYPE_JSON, CycleDefaultMimeType.JSON);
    artifactTypeJson.addDefaultContentRepresentation(new ContentRepresentationImpl(CONTENT_REPRESENTATION_ID_JSON, CycleDefaultMimeType.JSON, RenderInfo.CODE),
            new TextFileContentProvider());
    artifactTypeJson.addDownloadContentAction(CONTENT_REPRESENTATION_ID_JSON);
    types.add(artifactTypeJson);

}

  public Class< ? extends RepositoryConnectorConfiguration> getRepositoryConnectorConfigurationType() {
    return FileSystemConnectorConfiguration.class;
  }
}
