package org.activiti.rest.diagram.services;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Get;

public class ProcessInstanceHighlightsResource extends SecuredResource {

	private RuntimeService runtimeService = ActivitiUtil.getRuntimeService();
	private RepositoryServiceImpl repositoryService = (RepositoryServiceImpl) ActivitiUtil.getRepositoryService();
	private HistoryService historyService = (HistoryService) ActivitiUtil.getHistoryService();
	private ProcessInstance processInstance;
	private ProcessDefinitionEntity processDefinition;
	
	List<String> historicActivityInstanceList = new ArrayList<String>();
	List<String> highLightedFlows = new ArrayList<String>();

	@Get
	public ObjectNode getHighlighted() {
		String processInstanceId = (String) getRequest().getAttributes().get("processInstanceId");

		if (processInstanceId == null) {
			throw new ActivitiException("No process instance id provided");
		}

		ObjectNode responseJSON = new ObjectMapper().createObjectNode();
		
		responseJSON.put("processInstanceId", processInstanceId);
		
		ArrayNode activitiesArray = new ObjectMapper().createArrayNode();
		ArrayNode flowsArray = new ObjectMapper().createArrayNode();
		
		try {
			processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
			processDefinition = (ProcessDefinitionEntity) repositoryService.getDeployedProcessDefinition(processInstance.getProcessDefinitionId());
			
			responseJSON.put("processDefinitionId", processInstance.getProcessDefinitionId());
			
			List<String> highLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
			List<String> highLightedFlows = getHighLightedFlows(processDefinition, processInstanceId);
			
			for (String activityId : highLightedActivities)
				activitiesArray.add(activityId);
			
			for (String flow : highLightedFlows)
				flowsArray.add(flow);
			
			for (String activityId : highLightedActivities) {
				Execution execution = runtimeService.createExecutionQuery()
    				.processInstanceId(processInstance.getProcessInstanceId())
    				.activityId(activityId).singleResult();
				ExecutionEntity executionEntity = (ExecutionEntity)execution;
				executionEntity.getProcessDefinitionId();
			}
		} catch (Exception e) {}
		
		responseJSON.put("activities", activitiesArray);
		responseJSON.put("flows", flowsArray);
		
		return responseJSON;
	}
	
	// TODO: move this method to some 'utils'
	private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinition, String processInstanceId) {
		List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc()/*.orderByActivityId().asc()*/.list();
	    
    for (HistoricActivityInstance hai : historicActivityInstances) {
			historicActivityInstanceList.add(hai.getActivityId());
		}
	    
    // add current activities to list
    List<String> highLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
    historicActivityInstanceList.addAll(highLightedActivities);
 
    // activities and their sequence-flows
    getHighlightedFlows(processDefinition.getActivities());
    
    return highLightedFlows;
	}
	
	private void getHighlightedFlows(List<ActivityImpl> activityList) {
		for (ActivityImpl activity : activityList) {
      
      if (activity.getProperty("type").equals("subProcess")) {
    	  // get flows for the subProcess
        getHighlightedFlows(activity.getActivities());
      }
    	
      if (historicActivityInstanceList.contains(activity.getId())) {
    	  List<PvmTransition> pvmTransitionList = activity.getOutgoingTransitions();
    	  for (PvmTransition pvmTransition: pvmTransitionList) {
    		  String destinationFlowId = pvmTransition.getDestination().getId();
    		  if (historicActivityInstanceList.contains(destinationFlowId)) {
    			  highLightedFlows.add(pvmTransition.getId());
    		  }
    	  }
      }
    }
	}
}
