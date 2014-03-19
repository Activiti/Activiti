package org.activiti.validation.validator.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jbarrez
 */
public class ExclusiveGatewayValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<ExclusiveGateway> gateways = process.findFlowElementsOfType(ExclusiveGateway.class);
		for (ExclusiveGateway gateway : gateways) {
			validateExclusiveGateway(process, gateway, errors);
		}
	}
	
  public void validateExclusiveGateway(Process process, ExclusiveGateway exclusiveGateway, List<ValidationError> errors) {
    if (exclusiveGateway.getOutgoingFlows().size() == 0) {
    	addError(errors, Problems.EXCLUSIVE_GATEWAY_NO_OUTGOING_SEQ_FLOW, process, exclusiveGateway, 
    			"Exclusive gateway has no outgoing sequence flow");
    } else if (exclusiveGateway.getOutgoingFlows().size() == 1) {
    	SequenceFlow sequenceFlow = exclusiveGateway.getOutgoingFlows().get(0);
    	 if (StringUtils.isNotEmpty(sequenceFlow.getConditionExpression())) {
       	addError(errors, Problems.EXCLUSIVE_GATEWAY_CONDITION_NOT_ALLOWED_ON_SINGLE_SEQ_FLOW, process, exclusiveGateway, 
       			"Exclusive gateway has only one outgoing sequence flow. This is not allowed to have a condition.");
      }
    } else {
    	String defaultSequenceFlow = exclusiveGateway.getDefaultFlow();

      List<SequenceFlow> flowsWithoutCondition = new ArrayList<SequenceFlow>();
      for (SequenceFlow flow : exclusiveGateway.getOutgoingFlows()) {
      	String condition = flow.getConditionExpression();
      	boolean isDefaultFlow = flow.getId() != null && flow.getId().equals(defaultSequenceFlow);
      	boolean hasConditon = condition != null;
      	
        if (!hasConditon && !isDefaultFlow) {
          flowsWithoutCondition.add(flow);
        }
        if (hasConditon && isDefaultFlow) {
        	addError(errors, Problems.EXCLUSIVE_GATEWAY_CONDITION_ON_DEFAULT_SEQ_FLOW, process, exclusiveGateway, 
        			"Default sequenceflow has a condition, which is not allowed");
        }
      }
      
      if (flowsWithoutCondition.size() > 0) {
      	addWarning(errors, Problems.EXCLUSIVE_GATEWAY_SEQ_FLOW_WITHOUT_CONDITIONS, process, exclusiveGateway,
    				"Exclusive gateway has at least one outgoing sequence flow without a condition (which isn't the default one)");
    	}
      
    }
  }

	
	

}
