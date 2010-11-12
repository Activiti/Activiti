package org.activiti.cycle.impl.transform.signavio;

import java.util.ArrayList;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.activiti.cycle.impl.transform.JsonTransformationException;
import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.DiagramBuilder;
import org.oryxeditor.server.diagram.Shape;
import org.oryxeditor.server.diagram.StencilType;
import org.restlet.Response;


public class SubProcessExpansion extends OryxTransformation {

  public static final String STENCIL_SUBPROCESS = "Subprocess";
  public static final String STENCIL_COLLAPSED_SUBPROCESS = "CollapsedSubprocess";

  public static final String PROPERTY_NAME = "name";
  public static final String PROPERTY_ENTRY = "entry";

  private SignavioConnector signavioConnector;
  
  public SubProcessExpansion() {
    init(null);
  }

  public SubProcessExpansion(RepositoryConnector connector) {
    init(connector);
  }

  private void init(RepositoryConnector connector) {
    if (connector != null && connector instanceof SignavioConnector) {
      this.signavioConnector = (SignavioConnector) connector;
    } else {
      this.signavioConnector = new SignavioConnector(null);
    }
  }

  @Override
  public Diagram transform(Diagram diagram) {
    for (Shape shape : diagram.getShapes()) {
      if (STENCIL_COLLAPSED_SUBPROCESS.equals(shape.getStencilId())) {
        String subProcessUrl = shape.getProperty(PROPERTY_ENTRY);
        if (subProcessUrl != null && subProcessUrl.length() > 0) {
          try {
            Response jsonResponse = signavioConnector.getJsonResponse(subProcessUrl);
            String subProcessJson = jsonResponse.getEntity().getText();;
            Diagram subProcess = DiagramBuilder.parseJson(subProcessJson);
            shape.setStencil(new StencilType(STENCIL_SUBPROCESS));
            ArrayList<Shape> childShapes = shape.getChildShapes();
            childShapes.addAll(subProcess.getChildShapes());
          } catch (Exception e) {
            throw new JsonTransformationException(
                    "Error while retrieving Sub-Process"
                    + " '" + shape.getProperty(PROPERTY_NAME) + "'"
                    + " (URL: " + subProcessUrl + ").",
                    e);
          }
        }
      }
    }
    return diagram;
  }

}
