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
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.workflow.simple.converter.ConversionConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.definition.form.BooleanPropertyDefinition;
import org.activiti.workflow.simple.definition.form.DatePropertyDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyEntry;
import org.activiti.workflow.simple.definition.form.NumberPropertyDefinition;
import org.apache.commons.lang3.StringUtils;

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
public abstract class BaseStepDefinitionConverter<U extends StepDefinition, T> implements StepDefinitionConverter<U, T> {
  
  private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
  public T convertStepDefinition(StepDefinition stepDefinition, WorkflowDefinitionConversion conversion) {
    U typedStepDefinition = (U) stepDefinition;
    T processArtifact = createProcessArtifact(typedStepDefinition, conversion);
    createAdditionalArtifacts(conversion, typedStepDefinition, processArtifact);
    return processArtifact;
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
   * A sequence flow will NOT automatically be added
   */
  protected void addFlowElement(WorkflowDefinitionConversion conversion, FlowElement flowElement) {
    addFlowElement(conversion, flowElement, false);
  }

  protected void addFlowElement(WorkflowDefinitionConversion conversion, FlowElement flowElement, boolean addSequenceFlowToLastActivity) {
    if (conversion.isSequenceflowGenerationEnabled() && addSequenceFlowToLastActivity) {
      addSequenceFlow(conversion, conversion.getLastActivityId(), flowElement.getId());
    }
    conversion.getProcess().addFlowElement(flowElement);
    
    if (conversion.isUpdateLastActivityEnabled()) {
      conversion.setLastActivityId(flowElement.getId());
    }
  }
  
  protected SequenceFlow addSequenceFlow(WorkflowDefinitionConversion conversion, FlowNode sourceActivity, FlowNode targetActivity) {
    return addSequenceFlow(conversion, sourceActivity.getId(), targetActivity.getId());
  }

  protected SequenceFlow addSequenceFlow(WorkflowDefinitionConversion conversion, String sourceActivityId, String targetActivityId) {
    return addSequenceFlow(conversion, sourceActivityId, targetActivityId, null);
  }
  
  /**
   * Add a sequence-flow to the current process from source to target.
   * Sequence-flow name is set to a user-friendly name, containing an
   * incrementing number.
   *
   * @param conversion
   * @param sourceActivityId
   * @param targetActivityId
   * @param condition
   */
  protected SequenceFlow addSequenceFlow(WorkflowDefinitionConversion conversion, String sourceActivityId, 
      String targetActivityId, String condition) {
    
    SequenceFlow sequenceFlow = new SequenceFlow();
    sequenceFlow.setId(conversion.getUniqueNumberedId(getSequenceFlowPrefix()));
    sequenceFlow.setSourceRef(sourceActivityId);
    sequenceFlow.setTargetRef(targetActivityId);
    
    if (StringUtils.isNotEmpty(condition)) {
      sequenceFlow.setConditionExpression(condition);
    }

    conversion.getProcess().addFlowElement(sequenceFlow);
    return sequenceFlow;
  }
  
  // Subclasses can overwrite this if they want a different sequence flow prefix
  protected String getSequenceFlowPrefix() {
    return ConversionConstants.DEFAULT_SEQUENCEFLOW_PREFIX;
  }
  
  /**
   * Converts form properties. Multiple step types can contain forms,
   * hence why it it a shared method here.
   */
  protected List<FormProperty> convertProperties(FormDefinition formDefinition) {
    
    List<FormProperty> formProperties = new ArrayList<FormProperty>();
    
    for (FormPropertyDefinition propertyDefinition : formDefinition.getFormPropertyDefinitions()) {
      FormProperty formProperty = new FormProperty();
      formProperties.add(formProperty);
      
      formProperty.setId(propertyDefinition.getName());
      formProperty.setName(propertyDefinition.getName());
      formProperty.setRequired(propertyDefinition.isMandatory());
      
      String type = null;
      if (propertyDefinition instanceof NumberPropertyDefinition) {
        type = "long";
      } else if (propertyDefinition instanceof DatePropertyDefinition) {
        type = "date";
      } else if (propertyDefinition instanceof BooleanPropertyDefinition) {
        type = "boolean";
      } else if (propertyDefinition instanceof ListPropertyDefinition) {
        
        type = "enum";
        ListPropertyDefinition listDefinition = (ListPropertyDefinition) propertyDefinition;
        
        if (!listDefinition.getEntries().isEmpty()) {
          List<FormValue> formValues = new ArrayList<FormValue>(listDefinition.getEntries().size());
          for (ListPropertyEntry entry : listDefinition.getEntries()) {
            FormValue formValue = new FormValue();
            // We're using same value for id and name for the moment
            formValue.setId(entry.getValue());
            formValue.setName(entry.getName());
            formValues.add(formValue);
          }
          formProperty.setFormValues(formValues);
        }
      } else {
      	// Fallback to simple text
        type = "string";
      }
      formProperty.setType(type);
    }
    
    return formProperties;
  }
  
}
