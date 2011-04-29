package org.activiti.cycle.impl.processsolution.representation;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.mimetype.HtmlMimeType;
import org.activiti.cycle.impl.processsolution.artifacttype.ProcessSolutionHomeArtifactType;
import org.activiti.cycle.impl.util.IoUtils;

/**
 * Default {@link ContentRepresentation} for the "Home" screen
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class ProcessSolutionHomeContentRepresentation implements ContentRepresentation {

  private static final long serialVersionUID = 1L;

  public String getId() {
    return "Home";
  }

  public RenderInfo getRenderInfo() {
    return RenderInfo.HTML;
  }

  public Content getContent(RepositoryArtifact artifact) {
    try {
      String parsedForm = IoUtils.readText(getClass().getResourceAsStream("ProcessSolutionHomeContentRepresentation.html"));
      Content content = new Content();
      content.setValue(parsedForm);
      return content;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  public MimeType getRepresentationMimeType() {
    return CycleComponentFactory.getCycleComponentInstance(HtmlMimeType.class, HtmlMimeType.class);
  }

  public RepositoryArtifactType getRepositoryArtifactType() {
    return CycleComponentFactory.getCycleComponentInstance(ProcessSolutionHomeArtifactType.class, ProcessSolutionHomeArtifactType.class);
  }

  public boolean isForDownload() {
    return false;
  }

}
