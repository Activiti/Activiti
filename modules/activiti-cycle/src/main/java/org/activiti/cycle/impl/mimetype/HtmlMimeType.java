package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * Html Mimetype
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class HtmlMimeType extends AbstractMimeType {

  public String getName() {
    return "text/html";
  }

  public String[] getCommonFileExtensions() {
    return new String[] { "htm", "html" };
  }

}
