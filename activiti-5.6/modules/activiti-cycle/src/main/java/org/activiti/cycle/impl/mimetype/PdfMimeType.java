package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * PDF Mimetype
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class PdfMimeType extends AbstractMimeType {

  public String getName() {
    return "Pdf";
  }

  public String getContentType() {
    return "application/pdf";
  }

  public String[] getCommonFileExtensions() {
    return new String[] { "pdf" };
  }

}
