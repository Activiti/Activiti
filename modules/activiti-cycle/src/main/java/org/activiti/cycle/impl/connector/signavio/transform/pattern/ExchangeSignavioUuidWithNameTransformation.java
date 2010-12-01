package org.activiti.cycle.impl.connector.signavio.transform.pattern;

import java.util.List;

import org.activiti.cycle.impl.connector.signavio.transform.JsonTransformationException;
import org.activiti.cycle.impl.connector.signavio.util.CustomProperty;
import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.Shape;

/**
 * Transformation to exchange the Signavio UUIDs with real names, which makes it
 * much more handy in development when handling the BPMN 2.0 XML in source
 * 
 * Assumes that names are not empty.
 * You can use a {@link ReplaceEmptyShapeNamesWithTypes} to achieve that.
 * 
 * This transformation will use a combination of
 * {@link AdjustShapeNamesForXmlNCName} and {@link MakeNamesUnique}
 * to make names unique and compatible with XML NCNames
 * 
 * @author bernd.ruecker@camunda.com
 * @author Falko Menge
 */
public class ExchangeSignavioUuidWithNameTransformation extends OryxTransformation {

  private MakeNamesUnique makeNamesUnique = new MakeNamesUnique("_", "");

  @Override
  public Diagram transform(Diagram diagram) {
    makeNamesUnique.reset();
    adjustShapeNames(diagram.getShapes());
    return diagram;
  }

  private void adjustShapeNames(List<Shape> shapes) {
    for (Shape shape : shapes) {
      String id = shape.getResourceId();
      if (shape.getProperty("name") != null && shape.getProperty("name").length() > 0) {
        // backup original id
        String documentation = shape.getProperty("documentation");
        documentation = CustomProperty.ORIGINAL_ID.setValueUnlessPropertyExists(documentation, id);
        shape.getProperties().put("documentation", documentation);
        // generate id from name
        id = AdjustShapeNamesForXmlNCName.adjustForXmlNCName(shape.getProperty("name"));
        id = makeNamesUnique.transformName(id, shape);
        shape.setResourceId(id);
//      } else {
//        throw new JsonTransformationException("The Signavio shape has no name. (Type: " + shape.getStencilId() + ", ID: '" + id + "')");
      }
      adjustShapeNames(shape.getChildShapes());
    }
  }

}
