package org.activiti.cycle.impl.connector.signavio.transform.pattern;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.oryxeditor.server.diagram.Shape;

/**
 * Uses the type of an unnamed element as its name.
 * 
 * @author Falko Menge
 */
public class ReplaceEmptyShapeNamesWithTypes extends OryxShapeNameTransformation {

  private Set<String> exceptions = new HashSet<String>();

  public ReplaceEmptyShapeNamesWithTypes() {
    super();
  }
  
  public ReplaceEmptyShapeNamesWithTypes(String[] exceptions) {
    super();
    this.exceptions.addAll(Arrays.asList(exceptions));
  }

  public ReplaceEmptyShapeNamesWithTypes(Set<String> exceptions) {
    super();
    this.exceptions = exceptions;
  }

  @Override
  public String transformName(String name, Shape shape) {
    if (name == null || name.length() == 0) {
      String stencilId = shape.getStencilId();
      // TODO: there seems to be a problem with association not getting a name
      // even if they are not excluded
      if (!exceptions.contains(stencilId)) {
        name = stencilId;
      }
    }
    return name;
  }

}
