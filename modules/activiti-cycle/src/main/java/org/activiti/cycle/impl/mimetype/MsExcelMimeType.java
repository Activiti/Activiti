package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * MS_EXCEL Mimetype
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class MsExcelMimeType extends AbstractMimeType {

  public String getName() {
    return "Excel";
  }

  public String getContentType() {
    return "application/excel";
  }

  public String[] getCommonFileExtensions() {
    return new String[] { "xls" };
  }

}
