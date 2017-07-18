package org.activiti.bpmn.model.alfresco;

import org.activiti.bpmn.model.ServiceTask;

public class AlfrescoScriptTask extends ServiceTask {

  public static final String ALFRESCO_SCRIPT_DELEGATE = "org.alfresco.repo.workflow.activiti.script.AlfrescoScriptDelegate";
  public static final String ALFRESCO_SCRIPT_EXECUTION_LISTENER = "org.alfresco.repo.workflow.activiti.listener.ScriptExecutionListener";

  public AlfrescoScriptTask clone() {
    AlfrescoScriptTask clone = new AlfrescoScriptTask();
    clone.setValues(this);
    return clone;
  }

  public void setValues(AlfrescoScriptTask otherElement) {
    super.setValues(otherElement);
  }
}
