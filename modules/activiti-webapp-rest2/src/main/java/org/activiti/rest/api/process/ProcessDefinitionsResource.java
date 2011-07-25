package org.activiti.rest.api.process;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.ProcessDefinitionQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

public class ProcessDefinitionsResource extends SecuredResource {
  
  Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  public ProcessDefinitionsResource() {
    properties.put("id", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID);
    properties.put("key", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_KEY);
    properties.put("version", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_VERSION);
    properties.put("deploymentId", ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
    properties.put("name", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_NAME);
  }
  
  @Get
  public DataResponse getProcessDefinitions() {
    if(authenticate() == false) return null;

    DataResponse response = new ProcessDefinitionsPaginateList().paginateList(
        getQuery(), ActivitiUtil.getRepositoryService().createProcessDefinitionQuery(), "id", properties);
    return response;
  }
}
