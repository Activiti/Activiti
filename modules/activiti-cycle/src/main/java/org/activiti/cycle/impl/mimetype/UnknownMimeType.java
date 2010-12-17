package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * Unknown MimeType indicating that the mimetype is unknown.
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class UnknownMimeType extends AbstractMimeType {

  public String getName() {
    return "unknown";
  }

  public String getContentType() {
    return "unknown";
  }

  public String[] getCommonFileExtensions() {
    return new String[0];
  }

}
