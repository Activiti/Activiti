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

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.workflow.simple.converter.ConversionConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.StepDefinition;

/**
 * Base class that can be used for {@link StepDefinitionConverter}, contains utility-methods.
 * 
 * All {@link StepDefinitionConverter} should extend this class and implement any BPMN 2.0 xml
 * generation logic in the {@link #createProcessArtifact(StepDefinition, WorkflowDefinitionConversion)} method.
 * 
 * The generation of additional artifacts should be done by overriding the {@link #createAdditionalArtifacts(Object)}
 * method adn adding the produced artifacts to the generic map on the {@link WorkflowDefinitionConversion}.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public abstract class BaseStepDefinitionConverter<U extends StepDefinition, T> implements StepDefinitionConverter {
  
  @SuppressWarnings("unchecked")
  public void convertStepDefinition(StepDefinition stepDefinition, WorkflowDefinitionConversion conversion) {
    U typedStepDefinition = (U) stepDefinition;
    T processArtifact = createProcessArtifact(typedStepDefinition, conversion);
    createAdditionalArtifacts(conversion, typedStepDefinition, processArtifact);
  }
  
  /**
   * Subclasses must implement this method and create the BPMN 2.0 process artifact(s) for the provided step. 
   */
  protected abstract T createProcessArtifact(U stepDefinition, WorkflowDefinitionConversion conversion);

  /**
   * Subclasses should override this method if they want to create additional artifacts
   * for this specific step. The default generated process artifact is passed as parameter.
   */
  protected void createAdditionalArtifacts(WorkflowDefinitionConversion conversion, U stepDefinition, T defaultGeneratedArtifact) {
  }

  /**
   * Adds a flow element to the {@link Process}.
   * A sequence flow from the last known element to this element will be generated,
   * unless the sequence flow generation is globally disabled.
   */
  protected void addFlowElement(WorkflowDefinitionConversion conversion, FlowElement flowElement) {
      addFlowElement(conversion, flowElement, true);
  }

  protected void addFlowElement(WorkflowDefinitionConversion conversion, FlowElement flowElement, boolean addSequenceFlow) {
      if (conversion.isSequenceflowGenerationEnabled() && addSequenceFlow) {
          addSequenceFlow(conversion, conversion.getLastActivityId(), flowElement.getId());
      }
      conversion.getProcess().addFlowElement(flowElement);
      
      if (conversion.isUpdateLastActivityEnabled()) {
        conversion.setLastActivityId(flowElement.getId());
      }
  }

  /**
   * Add a sequence-flow to the current process from source to target.
   * Sequence-flow name is set to a user-friendly name, containing an
   * incrementing number.
   *
   * @param sourceActivityId
   * @param targetActivityId
   */
  public void addSequenceFlow(WorkflowDefinitionConversion conversion, String sourceActivityId, String targetActivityId) {
      SequenceFlow sequenceFlow = new SequenceFlow();
      sequenceFlow.setId(conversion.getUniqueNumberedId(getSequenceFlowPrefix()));
      sequenceFlow.setSourceRef(sourceActivityId);
      sequenceFlow.setTargetRef(targetActivityId);

      conversion.getProcess().addFlowElement(sequenceFlow);
  }
    
  // Subclasses can overwrite this if they want a different sequence flow prefix
  protected String getSequenceFlowPrefix() {
    return ConversionConstants.DEFAULT_SEQUENCEFLOW_PREFIX;
  }
  
}
