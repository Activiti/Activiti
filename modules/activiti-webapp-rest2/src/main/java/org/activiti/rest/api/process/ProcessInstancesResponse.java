package org.activiti.rest.api.process;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.rest.api.RequestUtil;

public class ProcessInstancesResponse {
  
  String id;
  String businessKey;
  String processDefinitionId;
  String startTime;
  
  public ProcessInstancesResponse(HistoricProcessInstance processInstance) {
    this.setId(processInstance.getId());
    this.setBusinessKey(processInstance.getBusinessKey());
    this.setStartTime(RequestUtil.dateToString(processInstance.getStartTime()));
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

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
}
