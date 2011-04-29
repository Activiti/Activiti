package org.activiti.cycle.impl.connector.signavio.transform.pattern;

import org.oryxeditor.server.diagram.Shape;

/**
 * Adjusts names from Signavio, i.e., removes all non special characters to avoid
 * possible problems.
 * 
 * @see http://forums.activiti.org/en/viewtopic.php?f=4&t=259&p=917
 * 
 * @author Bernd Ruecker
 * @author Falko Menge
 */
public class AdjustShapeNamesForXmlNCName extends OryxShapeNameTransformation {

  @Override
  public String transformName(String name, Shape shape) {
    return adjustForXmlNCName(name);
  }

  public static String adjustForXmlNCName(String name) {
    if (name != null) {
      name = name.replaceAll("[^a-zA-Z0-9-]", "_");
    }
    return name;
  }

}
