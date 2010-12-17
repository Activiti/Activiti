package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * JPEG Mimetype
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class JpegMimeType extends AbstractMimeType {

  public String getName() {
    return "Jpeg";
  }

  public String getContentType() {
    return "image/jpeg";
  }

  public String[] getCommonFileExtensions() {
    return new String[] { "jpeg", "jpg" };
  }

}
