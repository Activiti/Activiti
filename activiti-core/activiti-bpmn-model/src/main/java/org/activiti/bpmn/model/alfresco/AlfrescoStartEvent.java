package org.activiti.bpmn.model.alfresco;

import org.activiti.bpmn.model.StartEvent;

public class AlfrescoStartEvent extends StartEvent {

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

  public AlfrescoStartEvent clone() {
    AlfrescoStartEvent clone = new AlfrescoStartEvent();
    clone.setValues(this);
    return clone;
  }

  public void setValues(AlfrescoStartEvent otherElement) {
    super.setValues(otherElement);
    setRunAs(otherElement.getRunAs());
    setScriptProcessor(otherElement.getScriptProcessor());
  }
}
