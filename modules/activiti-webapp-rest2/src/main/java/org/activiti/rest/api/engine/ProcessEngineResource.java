package org.activiti.rest.api.engine;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineInfo;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

public class ProcessEngineResource extends SecuredResource {

  @Get
  public ProcessEngineInfoResponse getEngineInfo() {
    if(authenticate() == false) return null;
    
    ProcessEngineInfo engineInfo = ActivitiUtil.getProcessEngineInfo();
    ProcessEngineInfoResponse response = new ProcessEngineInfoResponse();
    response.setName(engineInfo.getName());
    response.setResourceUrl(engineInfo.getResourceUrl());
    response.setException(engineInfo.getException());
    response.setVersion(ProcessEngine.VERSION);
    return response;
  }
}