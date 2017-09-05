package org.activiti.bpmn.model.alfresco;

import org.activiti.bpmn.model.UserTask;

public class AlfrescoUserTask extends UserTask {

  public static final String ALFRESCO_SCRIPT_TASK_LISTENER = "org.alfresco.repo.workflow.activiti.tasklistener.ScriptTaskListener";

  protected String runAs;
  protected String scriptProcessor;

  public String getRunAs() {
    return runAs;
  }

  public void setRunAs(String runAs) {
    this.runAs = runAs;
  }

  public String getScriptProcessor() {
    return scriptProcessor;
  }

  public void setScriptProcessor(String scriptProcessor) {
    this.scriptProcessor = scriptProcessor;
  }

  public AlfrescoUserTask clone() {
    AlfrescoUserTask clone = new AlfrescoUserTask();
    clone.setValues(this);
    return clone;
  }

  public void setValues(AlfrescoUserTask otherElement) {
    super.setValues(otherElement);
    setRunAs(otherElement.getRunAs());
    setScriptProcessor(otherElement.getScriptProcessor());
  }
}
