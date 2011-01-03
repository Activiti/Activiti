package org.activiti.cycle.impl.connector.signavio.provider;

import org.activiti.cycle.Content;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.components.RuntimeConnectorList;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;
import org.activiti.cycle.impl.connector.signavio.util.SignavioSvgApiBuilder;
import org.activiti.cycle.impl.mimetype.HtmlMimeType;

@CycleComponent(context = CycleContextType.APPLICATION)
public class SvgApiProvider extends SignavioContentRepresentationProvider {

  private static final long serialVersionUID = 1L;

  public Content getContent(RepositoryArtifact artifact) {
    SignavioConnectorInterface signavioConnector = (SignavioConnectorInterface) CycleSessionContext.get(RuntimeConnectorList.class).getConnectorById(
            artifact.getConnectorId());
    Content content = new Content();
    String text = new SignavioSvgApiBuilder(signavioConnector, artifact).buildHtml();
    content.setValue(text);
    return content;
  }

  public String getId() {
    return "SvgApi";
  }

  public MimeType getRepresentationMimeType() {
    return CycleApplicationContext.get(HtmlMimeType.class);
  }

  public RenderInfo getRenderInfo() {
    return RenderInfo.HTML;
  }

  public RepositoryArtifactType getRepositoryArtifactType() {
    return CycleApplicationContext.get(SignavioBpmn20ArtifactType.class);
  }

  public boolean isForDownload() {
    return false;
  }

}
