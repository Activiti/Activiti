package org.activiti.cycle.impl.connector.signavio.action;

import org.activiti.cycle.impl.connector.signavio.SignavioPluginDefinition;

/**
 * This action copies a Signavio model to another repository (ideally another
 * Signavio).
 * 
 * @author bernd.ruecker@camunda.com
 */
public class CopySignavioModelAction extends AbstractCopyBaseAction {

  private static final long serialVersionUID = 1L;  

  public CopySignavioModelAction() {
    // TODO: remove when real labels are introduced in the GUI
    super("Copy model");
  }  

  public String getContentRepresentationIdToUse() {
    return SignavioPluginDefinition.CONTENT_REPRESENTATION_ID_JSON;
  }

}
