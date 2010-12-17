package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * MS_POWERPOINT Mimetype
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class MsPowerpointMimeType extends AbstractMimeType {

  public String getName() {
    return "Powerpoint";
  }

  public String getContentType() {
    return "application/powerpoint";
  }

  public String[] getCommonFileExtensions() {
    return new String[] { "ppt", "pptx" };
  }
}
