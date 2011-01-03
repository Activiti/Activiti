package org.activiti.cycle.impl.connector.signavio.transform.pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.cycle.impl.connector.signavio.transform.JsonTransformationException;
import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.Shape;

/**
 * Replaces Intermediate Link Events by Sequence Flows.
 * 
 * Known Limitations:
 * Catching Intermediate Events are assumed to have exactly one outgoing Sequence Flow.
 * 
 * @author Falko Menge
 */
public class LinkResolver extends OryxTransformation {

  @Override
  public Diagram transform(Diagram diagram) {
    List<Shape> shapes = diagram.getShapes();
    Set<Shape> shapesToRemove = new HashSet<Shape>();
    for (Shape shape : shapes) {
      if ("IntermediateLinkEventThrowing".equals(shape.getStencilId())) {
        boolean targetFound = false;
        ArrayList<Shape> incomings = shape.getIncomings();
        for (Shape incomingShape : incomings) {
          if ("SequenceFlow".equals(incomingShape.getStencilId())) {
            for (Shape shape2 : shapes) {
              if ("IntermediateLinkEventCatching".equals(shape2.getStencilId())
                      && shape.getProperty("name") != null
                      && shape.getProperty("name").equals(shape2.getProperty("name")) ) {
                for (Shape outgoingShape : shape2.getOutgoings()) {
                  if ("SequenceFlow".equals(outgoingShape.getStencilId())) {
                    incomingShape.setTarget(outgoingShape.getTarget());
                    targetFound = true;
                    // FIXME support for multiple outgoing Sequence Flows (currently the last one will win)
                  }
                  shapesToRemove.add(outgoingShape);
                }
                shapesToRemove.add(shape2);
              }
            }
          } else {
            shapesToRemove.add(incomingShape);
          }
        }
        if (!targetFound) {
          throw new JsonTransformationException(
                  "Link target not found (Name: '" + shape.getProperty("name") + "', "
                  + "ID: '" + shape.getResourceId() + "').");
        }
        shapesToRemove.add(shape);
      }
    }
    // dosn't work: shapes.removeAll(shapesToRemove);
    for (Shape shape : shapesToRemove) {
      shape.getParent().getChildShapes().remove(shape);
    }
    return diagram;
  }

}
