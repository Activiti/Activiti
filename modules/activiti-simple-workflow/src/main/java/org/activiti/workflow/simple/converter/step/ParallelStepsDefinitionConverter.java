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

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversionFactory;
import org.activiti.workflow.simple.definition.ListStepDefinition;
import org.activiti.workflow.simple.definition.ParallelStepsDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;

/**
 * {@link StepDefinitionConverter} for converting a {@link ParallelStepsDefinition} to the following BPMN 2.0 structure:
 * 
 *      __ t1___
 *      |       |
 *   + -- ...---+-
 *      |       |
 *      - txxx---
 * 
 * @author Joram Barrez
 */
public class ParallelStepsDefinitionConverter extends BaseStepDefinitionConverter<ParallelStepsDefinition, ParallelGateway> {
  
  private static final long serialVersionUID = 1L;
  
	private static final String PARALLEL_GATEWAY_PREFIX = "parallelGateway";

  public Class< ? extends StepDefinition> getHandledClass() {
    return ParallelStepsDefinition.class;
  }

  protected ParallelGateway createProcessArtifact(ParallelStepsDefinition parallelStepsDefinition, WorkflowDefinitionConversion conversion) {

    // First parallel gateway
    ParallelGateway forkGateway = createParallelGateway(conversion);
    
    // Sequence flow from last activity to first gateway
    addSequenceFlow(conversion, conversion.getLastActivityId(), forkGateway.getId());
    conversion.setLastActivityId(forkGateway.getId());

    // Convert all other steps, disabling activity id updates which makes all 
    // generated steps have a sequence flow to the first gateway
    WorkflowDefinitionConversionFactory conversionFactory = conversion.getConversionFactory();
    List<FlowElement> endElements = new ArrayList<FlowElement>();
    for (ListStepDefinition<ParallelStepsDefinition> stepListDefinition : parallelStepsDefinition.getStepList()) {
      
      for (int i = 0; i < stepListDefinition.getSteps().size(); i++) {
        if (i == 0) {
          conversion.setSequenceflowGenerationEnabled(false);
        } else {
          conversion.setSequenceflowGenerationEnabled(true);
        }
        StepDefinition step = stepListDefinition.getSteps().get(i);
        FlowElement flowElement = (FlowElement) conversionFactory.getStepConverterFor(step).convertStepDefinition(step, conversion);
        
        if (i == 0) {
          addSequenceFlow(conversion, forkGateway.getId(), flowElement.getId());
        }
        
        if ((i + 1) == stepListDefinition.getSteps().size()) {
          endElements.add(flowElement);
        }
      }
    }
    
    conversion.setSequenceflowGenerationEnabled(false);
    
    // Second parallel gateway
    ParallelGateway joinGateway = createParallelGateway(conversion);
    conversion.setLastActivityId(joinGateway.getId());
    
    conversion.setSequenceflowGenerationEnabled(true);
    
    // Create sequence flow from all generated steps to the second gateway
    for (FlowElement endElement : endElements) {
      addSequenceFlow(conversion, endElement.getId(), joinGateway.getId());
    }
    
    return forkGateway;
  }
  
  protected ParallelGateway createParallelGateway(WorkflowDefinitionConversion conversion) {
    ParallelGateway parallelGateway = new ParallelGateway();
    parallelGateway.setId(conversion.getUniqueNumberedId(PARALLEL_GATEWAY_PREFIX));
    conversion.getProcess().addFlowElement(parallelGateway);
    return parallelGateway;
  }
  
}
