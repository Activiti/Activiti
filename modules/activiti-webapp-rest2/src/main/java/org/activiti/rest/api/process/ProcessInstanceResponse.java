package org.activiti.rest.api.process;

import org.activiti.engine.runtime.ProcessInstance;

public class ProcessInstanceResponse {
  
  String id;
  String businessKey;
  String processInstanceId;
  String processDefinitionId;
  
  public ProcessInstanceResponse(ProcessInstance processInstance) {
    this.setId(processInstance.getId());
    this.setBusinessKey(processInstance.getBusinessKey());
    this.setProcessInstanceId(processInstance.getProcessInstanceId());
    this.setProcessDefinitionId(processInstance.getProcessDefinitionId());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
}
