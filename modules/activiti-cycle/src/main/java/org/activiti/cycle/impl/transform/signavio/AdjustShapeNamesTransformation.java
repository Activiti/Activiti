package org.activiti.cycle.impl.transform.signavio;

import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.Shape;

/**
 * adjust name from Signavio (remove new lines, ' and maybe add more in future)
 * See https://app.camunda.com/jira/browse/HEMERA-164.
 * 
 * @author bernd.ruecker@camunda.com
 */
public class AdjustShapeNamesTransformation extends OryxTransformation {

  @Override
	public Diagram transform(Diagram diagram) {
    for (Shape shape : diagram.getShapes()) {
      if (shape.getProperties().containsKey("name")) {
        String name = shape.getProperty("name");
        String newName = name.replaceAll("\n", " ").replaceAll("'", "").replaceAll("\"", "");
        shape.getProperties().put("name", newName);
			}
		}
    
		return diagram;
  }

}
