/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.workflow.simple.converter.step;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversionFactory;
import org.activiti.workflow.simple.definition.ChoiceStepsDefinition;
import org.activiti.workflow.simple.definition.ConditionDefinition;
import org.activiti.workflow.simple.definition.ListConditionStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;

/**
 * {@link StepDefinitionConverter} for converting a {@link ChoiceStepsDefinition} to the following BPMN 2.0 structure:
 * 
 *      __ t1___
 *      |       |
 *   + -- ...---+-
 *      |       |
 *      - txxx---
 * 
 * @author Tijs Rademakers
 */
public class ChoiceStepsDefinitionConverter extends BaseStepDefinitionConverter<ChoiceStepsDefinition, ExclusiveGateway> {
  
  private static final long serialVersionUID = 1L;
  
	private static final String EXLCUSIVE_GATEWAY_PREFIX = "exclusiveGateway";

  public Class< ? extends StepDefinition> getHandledClass() {
    return ChoiceStepsDefinition.class;
  }

  protected ExclusiveGateway createProcessArtifact(ChoiceStepsDefinition choiceStepsDefinition, WorkflowDefinitionConversion conversion) {

    // First choice gateway
    ExclusiveGateway forkGateway = createExclusiveGateway(conversion);
    
    // Sequence flow from last activity to first gateway
    addSequenceFlow(conversion, conversion.getLastActivityId(), forkGateway.getId());
    conversion.setLastActivityId(forkGateway.getId());

    // Convert all other steps, disabling activity id updates which makes all 
    // generated steps have a sequence flow to the first gateway
    WorkflowDefinitionConversionFactory conversionFactory = conversion.getConversionFactory();
    List<FlowElement> endElements = new ArrayList<FlowElement>();
    List<SequenceFlow> bypassingFlows = new ArrayList<SequenceFlow>();
    for (ListConditionStepDefinition<ChoiceStepsDefinition> stepListDefinition : choiceStepsDefinition.getStepList()) {
      
      StringBuilder conditionBuilder = new StringBuilder();
      for (ConditionDefinition conditionDefintion : stepListDefinition.getConditions()) {
        if (conditionBuilder.length() > 0) {
          conditionBuilder.append(" && ");
        } else {
          conditionBuilder.append("${");
        }
        
        conditionBuilder.append(conditionDefintion.getLeftOperand());
        conditionBuilder.append(" ");
        conditionBuilder.append(conditionDefintion.getOperator());
        conditionBuilder.append(" ");
        conditionBuilder.append(conditionDefintion.getRightOperand());
      }
      
      for (int i = 0; i < stepListDefinition.getSteps().size(); i++) {
        if (i == 0) {
          conversion.setSequenceflowGenerationEnabled(false);
        } else {
          conversion.setSequenceflowGenerationEnabled(true);
        }
        StepDefinition step = stepListDefinition.getSteps().get(i);
        FlowElement flowElement = (FlowElement) conversionFactory.getStepConverterFor(step).convertStepDefinition(step, conversion);
        
        if (i == 0) {
          if (conditionBuilder.length() > 0) {
            conditionBuilder.append("}");
            SequenceFlow mainFlow = addSequenceFlow(conversion, forkGateway.getId(), flowElement.getId(), conditionBuilder.toString());
            if(stepListDefinition.getName() != null) {
            	mainFlow.setName(stepListDefinition.getName());
          	}
          } else {
            addSequenceFlow(conversion, forkGateway.getId(), flowElement.getId());
          }
        }
        
        if ((i + 1) == stepListDefinition.getSteps().size()) {
          endElements.add(flowElement);
        }
      }
      
      if(stepListDefinition.getSteps().isEmpty()) {
        // Special case for a "stepless" stepListDefinition, which should just create a sequence-flow from the fork to the join
      	SequenceFlow created = null;
      	if (conditionBuilder.length() > 0) {
          conditionBuilder.append("}");
          created = addSequenceFlow(conversion, forkGateway.getId(), null, conditionBuilder.toString());
      	} else {
      		created = addSequenceFlow(conversion, forkGateway.getId(), null);
      	}
      	if(stepListDefinition.getName() != null) {
      		created.setName(stepListDefinition.getName());
      	}
      	bypassingFlows.add(created);
      }
    }
    
    conversion.setSequenceflowGenerationEnabled(false);
    
    // Second choice gateway
    ExclusiveGateway joinGateway = createExclusiveGateway(conversion);
    conversion.setLastActivityId(joinGateway.getId());
    
    conversion.setSequenceflowGenerationEnabled(true);
    
    // Create sequenceflow from all generated steps to the second gateway
    for (FlowElement endElement : endElements) {
    	if(!(endElement instanceof EndEvent)) {
    		addSequenceFlow(conversion, endElement.getId(), joinGateway.getId());
    	}
    }
    
    for(SequenceFlow bypassingFlow : bypassingFlows) {
    	bypassingFlow.setTargetRef(joinGateway.getId());
    }
    
    return forkGateway;
  }
  
  protected ExclusiveGateway createExclusiveGateway(WorkflowDefinitionConversion conversion) {
    ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
    exclusiveGateway.setId(conversion.getUniqueNumberedId(EXLCUSIVE_GATEWAY_PREFIX));
    conversion.getProcess().addFlowElement(exclusiveGateway);
    return exclusiveGateway;
  }
  
}
