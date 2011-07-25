package org.activiti.rest.api.management;

import org.activiti.engine.runtime.Job;
import org.activiti.rest.api.RequestUtil;

public class JobResponse {

  String id;
  String executionId;
  String processInstanceId;
  String dueDate;
  int retries;
  String exceptionMessage;
  String stacktrace;
  
  public JobResponse(Job job) {
    setId(job.getId());
    setExecutionId(job.getExecutionId());
    setProcessInstanceId(job.getProcessInstanceId());
    setDueDate(RequestUtil.dateToString(job.getDuedate()));
    setRetries(job.getRetries());
    setExceptionMessage(job.getExceptionMessage());
  }
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getExecutionId() {
    return executionId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  public String getDueDate() {
    return dueDate;
  }
  public void setDueDate(String dueDate) {
    this.dueDate = dueDate;
  }
  public int getRetries() {
    return retries;
  }
  public void setRetries(int retries) {
    this.retries = retries;
  }
  public String getExceptionMessage() {
    return exceptionMessage;
  }
  public void setExceptionMessage(String exceptionMessage) {
    this.exceptionMessage = exceptionMessage;
  }
  public String getStacktrace() {
    return stacktrace;
  }
  public void setStacktrace(String stacktrace) {
    this.stacktrace = stacktrace;
  }
}
