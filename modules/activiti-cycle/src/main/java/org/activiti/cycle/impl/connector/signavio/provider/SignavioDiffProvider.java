package org.activiti.cycle.impl.connector.signavio.provider;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cycle.Content;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;
import org.activiti.cycle.impl.connector.signavio.util.SignavioSvgApiBuilder;
import org.activiti.cycle.impl.connector.signavio.util.SignavioSvgHighlight;
import org.activiti.cycle.impl.connector.signavio.util.SignavioSvgHighlightType;
import org.activiti.cycle.impl.connector.signavio.util.SignavioSvgNodeType;
import org.activiti.cycle.impl.mimetype.HtmlMimeType;
import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.DiagramBuilder;
import org.oryxeditor.server.diagram.Shape;

/**
 * <b>EXPERIMENTAL</b> proof of concept of an easy DIFF functionality with
 * Signavio within Cycle. Pretty hacky, so don't use this in real life
 * 
 * @author ruecker
 */
// un-comment to "turn on.
// @CycleComponent(context = CycleContextType.APPLICATION)
public class SignavioDiffProvider extends SignavioContentRepresentationProvider {

  // private static final String ERROR_COLOR = "red";
  // private static final String WARN_COLOR = "#FFE736";
  private static final String INFO_COLOR = "#8CC0ED";

  public static RepositoryArtifact targetArtifact;

  public String getId() {
    return "Diff";
  }

  public RenderInfo getRenderInfo() {
    return RenderInfo.HTML;
  }
  
  public MimeType getRepresentationMimeType() {
    return CycleApplicationContext.get(HtmlMimeType.class);
  }

  public RepositoryArtifactType getRepositoryArtifactType() {
    return CycleApplicationContext.get(SignavioBpmn20ArtifactType.class);
  }
  
  public boolean isForDownload() {
    return false;
  }

  public Content getContent(RepositoryArtifact artifact) {

    SignavioConnectorInterface connector = (SignavioConnectorInterface) CycleSessionContext.get(RuntimeConnectorList.class).getConnectorById(artifact.getConnectorId());
    Content content = new Content();

    RepositoryArtifact diffTarget = targetArtifact;
    if (targetArtifact == null) {
      // if nothing is selected we diff against ourself (boooring ;-))
      diffTarget = artifact;
    }

    List<SignavioSvgHighlight> missingSourceElements = new ArrayList<SignavioSvgHighlight>();
    List<SignavioSvgHighlight> missingTargetElements = new ArrayList<SignavioSvgHighlight>();

    // create DIFF
    String sourceJson = CycleApplicationContext.get(JsonProvider.class).getContent(artifact).asString();
    String targetJson = CycleApplicationContext.get(JsonProvider.class).getContent(diffTarget).asString();

    try {
      Diagram sourceDiagram = DiagramBuilder.parseJson(sourceJson);
      Diagram targetDiagram = DiagramBuilder.parseJson(targetJson);

      // First quick HACKY way to find missing elements on the top level
      for (Shape sourceShape : sourceDiagram.getShapes()) {
        String sourceId = sourceShape.getResourceId();
        // check if existant in target
        boolean existant = false;
        for (Shape targetShape : targetDiagram.getShapes()) {
          if (targetShape.getResourceId() != null && targetShape.getResourceId().equals(sourceId)) {
            existant = true;
          }
        }
        if (!existant) {
          // add to missing nodes in target artifact
          missingSourceElements.add(new SignavioSvgHighlight(SignavioSvgNodeType.NODE, SignavioSvgHighlightType.INFO, sourceId, "MISSING"));
        }
      }
      for (Shape targetShape : targetDiagram.getShapes()) {
        String targetId = targetShape.getResourceId();
        // check if existant in target
        boolean existant = false;
        for (Shape sourceShape : sourceDiagram.getShapes()) {
          if (sourceShape.getResourceId() != null && sourceShape.getResourceId().equals(targetId)) {
            existant = true;
          }
        }
        if (!existant) {
          // add to missing nodes in target artifact
          missingTargetElements.add(new SignavioSvgHighlight(SignavioSvgNodeType.NODE, SignavioSvgHighlightType.INFO, targetId, "MISSING"));
        }
      }
    } catch (Exception e) {
      throw new RepositoryException("Could not create DIFF due to exception", e);
    }

    // and create resulting HTML
    String script1 = new SignavioSvgApiBuilder(connector, artifact).highlightNodes(missingSourceElements).buildScript(75);
    String script2 = new SignavioSvgApiBuilder(connector, diffTarget).highlightNodes(missingTargetElements).buildScript(75);

    Integer height = 200;
    String htmlContent = "<p><b>Expertimental</b> feature to play around with Signavio diffing.<br/>Currently, showing diff against artifact "
            + diffTarget.getGlobalUniqueId() + ".<br/>Use the Actions menu to select another diff target.</p>";

    String additionalContent = "<p>Changes from " + diffTarget.getMetadata().getName() + " in " + artifact.getMetadata().getName();
    additionalContent += "<div id=\"model1\" style=\"height: " + height + "px;\">" + script1 + "</div>";
    additionalContent += "Changes from " + artifact.getMetadata().getName() + " in " + diffTarget.getMetadata().getName();
    additionalContent += "<div id=\"model2\" style=\"height: " + height + "px;\">" + script2 + "</div>";

    String html = new SignavioSvgApiBuilder(connector, artifact).buildHtml(htmlContent, additionalContent);
    content.setValue(html);
    return content;

  }
}
