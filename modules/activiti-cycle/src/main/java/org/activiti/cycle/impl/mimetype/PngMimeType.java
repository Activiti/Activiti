package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * PNG Mimetype
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class PngMimeType extends AbstractMimeType {

  public String getName() {
    return "image/png";
  }

  public String[] getCommonFileExtensions() {
    return new String[] { "png" };
  }

}
