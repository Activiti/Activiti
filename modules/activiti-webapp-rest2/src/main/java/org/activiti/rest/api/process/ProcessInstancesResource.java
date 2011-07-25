package org.activiti.rest.api.process;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.HistoricProcessInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

public class ProcessInstancesResource extends SecuredResource {
  
  Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  public ProcessInstancesResource() {
    properties.put("id", HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
    properties.put("processDefinitionId", HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
    properties.put("businessKey", HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
    properties.put("startTime", HistoricProcessInstanceQueryProperty.START_TIME);
  }
  
  @Get
  public DataResponse getProcessInstances() {
    if(authenticate() == false) return null;
    
    HistoricProcessInstanceQuery query = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery();
    query = query.unfinished();
    String processDefinitionId = getQuery().getValues("processDefinitionId");
    String processInstanceKey = getQuery().getValues("businessKey");
    query = processDefinitionId == null ? query : query.processDefinitionId(processDefinitionId);
    query = processInstanceKey == null ? query : query.processInstanceBusinessKey(processInstanceKey);
    
    DataResponse response = new ProcessInstancesPaginateList().paginateList(getQuery(), query, "id", properties);
    return response;
  }
}
