package org.activiti.cycle.impl.transform.signavio;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.Shape;

/**
 * Transformation to exchange the Signavio UUIDs with real names, which makes it
 * much more handy in development when handling the BPMN 2.0 XML in source
 * 
 * @author bernd.ruecker@camunda.com
 */
public class ExchangeSignavioUuidWithNameTransformation extends OryxTransformation {

  private static final String TASK_NAME = "Task";

  @Override
  public Diagram transform(Diagram diagram) {
    Set<String> existingNames = new HashSet<String>();

    adjustShapeNames(diagram.getShapes(), existingNames);

    return diagram;
  }

  private void adjustShapeNames(List<Shape> shapes, Set<String> existingNames) {
    for (Shape shape : shapes) {      
      // TODO: Check which exact stencil sets we need to change
      String name = null;
      
      if (shape.getProperty("name") != null && shape.getProperty("name").length() > 0) {
        // shape.getStencil()!= null &&/
        // TASK_NAME.equals(shape.getStencil().getId())
        name = shape.getProperty("name");

      } else {
        name = shape.getStencilId();
      }

      String newName = adjustNamesForEngine(name);

      if (existingNames.contains(newName)) {
        int counter = 1;
        while (existingNames.contains(newName + "_" + counter)) {
          counter++;
        }
        newName = newName + "_" + counter;
      }

      existingNames.add(newName);
      shape.setResourceId(newName);
      
      adjustShapeNames(shape.getChildShapes(), existingNames);
    }
  }

  /**
   * adjust name from Signavio (remove new lines, ' and maybe add more in
   * future) See https://app.camunda.com/jira/browse/HEMERA-164.
   * 
   * TODO: Should we have this as own pattern?
   */
  public static String adjustNamesForEngine(String name) {
    return name.replaceAll("\n", " ").replaceAll("'", "").replaceAll("\"", "");
  } 
}
