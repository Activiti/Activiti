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

import java.util.List;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.ParallelStepsDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.util.BpmnModelUtil;

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
    conversion.setUpdateLastActivityEnabled(false);
    conversion.convertSteps(parallelStepsDefinition.getSteps());
    conversion.setUpdateLastActivityEnabled(true);
    
    // Second parallel gateway
    ParallelGateway joinGateway = createParallelGateway(conversion);
    conversion.setLastActivityId(joinGateway.getId());
    
    // Create sequenceflow from all generated steps to the second gateway
    List<FlowElement> successorsOfFork = BpmnModelUtil.findSucessorFlowElementsFor(conversion.getProcess(), forkGateway);
    for (FlowElement successorOfFork : successorsOfFork) {
      addSequenceFlow(conversion, successorOfFork.getId(), joinGateway.getId());
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
