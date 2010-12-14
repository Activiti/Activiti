package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * MS_WORD Mimetype
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class MsWordMimeType extends AbstractMimeType {

  public String getName() {
    return "application/msword";
  }

  public String[] getCommonFileExtensions() {
    return new String[] { "doc", "docx" };
  }

}
