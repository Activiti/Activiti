package org.activiti.cycle.impl.connector;

import java.util.Map.Entry;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.annotations.ListAfterComponents;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.mimetype.HtmlMimeType;

/**
 * {@link ContentRepresentation} providing a tab with metadata 
 * 
 * @author Daniel Meyer
 */
@CycleComponent(context = CycleContextType.APPLICATION)
@ListAfterComponents
public class ArtifactMetadataContentRepresentation implements ContentRepresentation {

  private static final long serialVersionUID = 1L;

  public String getId() {
    return "Metadata";
  }

  public RenderInfo getRenderInfo() {
    return RenderInfo.HTML;
  }

  public Content getContent(RepositoryArtifact artifact) {
    Content content = new Content();
    content.setValue(generateMetadataTable(artifact));
    return content;
  }

  protected String generateMetadataTable(RepositoryArtifact artifact) {
    StringBuilder sb = new StringBuilder();
    sb.append("<h2>Metadata</h2>");
    sb.append("<table>");
    for (Entry<String, String> entry : artifact.getMetadata().getAsStringMap().entrySet()) {
      sb.append("<tr>");
      sb.append("<td><strong>");
      sb.append(entry.getKey());
      sb.append("</strong></td>");
      sb.append("<td>");
      sb.append(entry.getValue());
      sb.append("</td>");
      sb.append("</tr>");
    }
    sb.append("</table>");
    return sb.toString();
  }

  public MimeType getRepresentationMimeType() {
    return CycleComponentFactory.getCycleComponentInstance(HtmlMimeType.class);
  }

  public RepositoryArtifactType getRepositoryArtifactType() {
    return null; // null means for all
  }

  public boolean isForDownload() {
    return false;
  }

}
