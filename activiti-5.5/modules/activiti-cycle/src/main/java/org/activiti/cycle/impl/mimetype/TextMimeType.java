package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * Text Mimetype
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class TextMimeType extends AbstractMimeType {

  public String getName() {
    return "Text";
  }

  public String getContentType() {
    return "text/plain";
  }

  public String[] getCommonFileExtensions() {
    return new String[] { "txt" };
  }

}
