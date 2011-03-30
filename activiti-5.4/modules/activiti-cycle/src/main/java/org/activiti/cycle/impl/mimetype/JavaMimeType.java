package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.MimeType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * {@link MimeType} for Java Source Files
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class JavaMimeType extends AbstractMimeType {

  public String getName() {
    return "Java Source File";
  }

  public String getContentType() {
    return "text/java";
  }

  public String[] getCommonFileExtensions() {
    return new String[] { "java" };
  }

}
