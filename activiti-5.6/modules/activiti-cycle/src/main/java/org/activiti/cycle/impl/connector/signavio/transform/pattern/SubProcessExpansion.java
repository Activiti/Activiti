package org.activiti.cycle.impl.connector.signavio.transform.pattern;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.connector.signavio.transform.JsonTransformationException;
import org.activiti.cycle.impl.connector.signavio.util.CustomProperty;
import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.DiagramBuilder;
import org.oryxeditor.server.diagram.Shape;
import org.oryxeditor.server.diagram.StencilType;

/**
 * @author Falko Menge
 */
public class SubProcessExpansion extends OryxTransformation {

  public static final String STENCIL_SUBPROCESS = "Subprocess";
  public static final String STENCIL_COLLAPSED_SUBPROCESS = "CollapsedSubprocess";

  public static final String PROPERTY_NAME = "name";
  public static final String PROPERTY_ENTRY = "entry";
  public static final String PROPERTY_IS_CALL_ACTIVITY = "callacitivity";
  private static final String PROPERTY_DOCUMENTATION = "documentation";

  private RepositoryConnector connector;
  
  private Set<String> shapeIds;

  public SubProcessExpansion(RepositoryConnector repositoryConnector) {
    this.connector = repositoryConnector;
  }

  @Override
  public Diagram transform(Diagram diagram) {
    shapeIds = new HashSet<String>();
    ensureUniqueIds(diagram); // collect shapeIds of parent process
    expandSubProcesses(diagram);
    return diagram;
  }

  private void expandSubProcesses(Diagram diagram) {
    List<Shape> shapes = diagram.getShapes();
    for (Shape shape : shapes) {
      if (STENCIL_COLLAPSED_SUBPROCESS.equals(shape.getStencilId()) && !"true".equals(shape.getProperty(PROPERTY_IS_CALL_ACTIVITY))) {
        String subProcessUrl = shape.getProperty(PROPERTY_ENTRY);
        if (subProcessUrl != null && subProcessUrl.length() > 0) {
          String subProcessName = shape.getProperty(PROPERTY_NAME);
          try {
            String subProcessId = getModelIdFromSignavioUrl(subProcessUrl);

//            RepositoryArtifact artifact = connector.getRepositoryArtifact(subProcessId);
            String subProcessJson = connector.getContent(subProcessId).asString();
            
            Diagram subProcess = DiagramBuilder.parseJson(subProcessJson);

            // FIXME subProcess = new ExtractProcessOfParticipant("Process Engine").transform(subProcess); 
            
            ensureUniqueIds(subProcess);
            
            expandSubProcesses(subProcess);
            
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

  private void ensureUniqueIds(Diagram diagram) {
    for (Shape shape : diagram.getShapes()) {
      String id = shape.getResourceId();
      Integer numberOfDuplicates = 0;
      while (shapeIds.contains(id)) {
        id = shape.getResourceId() + "-copy-" + (++numberOfDuplicates); 
      }
      shapeIds.add(id);
      if (id != shape.getResourceId()) {
        changeShapeID(shape, id);
      }
    }
  }

  private void changeShapeID(Shape shape, String id) {
    String documentation = shape.getProperty(PROPERTY_DOCUMENTATION);
    documentation = CustomProperty.ORIGINAL_ID.setValueUnlessPropertyExists(documentation, shape.getResourceId());
    shape.getProperties().put(PROPERTY_DOCUMENTATION, documentation);
    shape.setResourceId(id);
  }

}
