package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * Gif Mimetype
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class GifMimeType extends AbstractMimeType {

  public String getName() {
    return "Gif";
  }
  
  public String getContentType() {
    return "image/gif";
  }

  public String[] getCommonFileExtensions() {
    return new String[] { "gif" };
  }

}
