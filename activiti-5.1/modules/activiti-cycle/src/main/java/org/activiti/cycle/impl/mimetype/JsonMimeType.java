package org.activiti.cycle.impl.mimetype;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * JSON Mimetype
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class JsonMimeType extends AbstractMimeType {

  public String getName() {
    return "Json";
  }
  
  public String getContentType() {
    return "application/json;charset=UTF-8";
  }

  public String[] getCommonFileExtensions() {
    return new String[] { "json" };
  }
  
}
