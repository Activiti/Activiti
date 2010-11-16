package org.activiti.cycle.impl.transform.signavio;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.connector.signavio.SignavioPluginDefinition;
import org.activiti.cycle.impl.transform.JsonTransformationException;
import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.DiagramBuilder;
import org.oryxeditor.server.diagram.Shape;
import org.oryxeditor.server.diagram.StencilType;


public class SubProcessExpansion extends OryxTransformation {

  public static final String STENCIL_SUBPROCESS = "Subprocess";
  public static final String STENCIL_COLLAPSED_SUBPROCESS = "CollapsedSubprocess";

  public static final String PROPERTY_NAME = "name";
  public static final String PROPERTY_ENTRY = "entry";

  private RepositoryConnector connector;
  
  public SubProcessExpansion(RepositoryConnector repositoryConnector) {
    this.connector = repositoryConnector;
  }

  @Override
  public Diagram transform(Diagram diagram) {
    for (Shape shape : diagram.getShapes()) {
      if (STENCIL_COLLAPSED_SUBPROCESS.equals(shape.getStencilId())) {
        String subProcessUrl = shape.getProperty(PROPERTY_ENTRY);
        if (subProcessUrl != null && subProcessUrl.length() > 0) {
          String subProcessName = shape.getProperty(PROPERTY_NAME);
          try {
            String subProcessId = getModelIdFromSignavioUrl(subProcessUrl);

            String representationName = SignavioPluginDefinition.CONTENT_REPRESENTATION_ID_JSON;
            String subProcessJson = connector.getContent(subProcessId, representationName).asString();
            
            Diagram subProcess = DiagramBuilder.parseJson(subProcessJson);
            shape.setStencil(new StencilType(STENCIL_SUBPROCESS));
            ArrayList<Shape> childShapes = shape.getChildShapes();
            childShapes.addAll(subProcess.getChildShapes());
          } catch (Exception e) {
            throw new JsonTransformationException(
                    "Error while retrieving Sub-Process"
                    + " '" + subProcessName + "'"
                    + " (URL: " + subProcessUrl + ").",
                    e);
          }
        }
      }
    }
    return diagram;
  }

  public static String getModelIdFromSignavioUrl(String subProcessUrl) throws UnsupportedEncodingException {
    String modelId = null;
    List<Pattern> patterns = new ArrayList<Pattern>();
    patterns.add(Pattern.compile("^.*/p/model/(.*)$"));
    patterns.add(Pattern.compile("^.*/p/editor[?]id=(.*)$")); // workaround for Activiti Modeler
    for (Pattern pattern : patterns) {
      Matcher matcher = pattern.matcher(subProcessUrl);
      if (matcher.matches()) {
        modelId = URLDecoder.decode(matcher.group(1), "UTF-8");
        break;
      }
    }
    return modelId;
  }

}
