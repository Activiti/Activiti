package org.activiti.bpmn.converter.child;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;

public class ActivitiFailedjobRetryParser extends BaseChildElementParser {

  @Override
  public String getElementName() {
    return FAILED_JOB_RETRY_TIME_CYCLE;
  }

  @Override
  public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
    if (!(parentElement instanceof Activity))
      return;
    String cycle = xtr.getElementText();
    if (cycle == null || cycle.isEmpty()) {
      return;
    }
    ((Activity) parentElement).setFailedJobRetryTimeCycleValue(cycle);
  }

}
