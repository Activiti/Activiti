package org.activiti.cycle.impl.connector.signavio.transform.pattern;

import java.util.HashSet;
import java.util.Set;

import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.Shape;

/**
 * @author Falko Menge
 */
public class MakeNamesUnique extends OryxShapeNameTransformation {

  private String prefix = " (";
  private String suffix = ")";
  private Set<String> existingNames = new HashSet<String>();

  public MakeNamesUnique() {
  }

  public MakeNamesUnique(String prefix, String suffix) {
    this.prefix = prefix;
    this.suffix = suffix;
  }

  @Override
  public Diagram transform(Diagram diagram) {
    reset();
    return super.transform(diagram);
  }

  public void reset() {
    existingNames = new HashSet<String>();
  }
  
  @Override
  public String transformName(String name, Shape shape) {
    int counter = 1;
    String newName = name;
    while (existingNames.contains(newName)) {
      counter++;
      newName = name + prefix + counter + suffix;
    }
    existingNames.add(newName);
    return newName;
  }

}
