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
package org.activiti.workflow.simple.converter;

import java.util.HashMap;
import java.util.List;

import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.definition.WorkflowDefinition;

/**
 * Factory that is capable of creating {@link WorkflowDefinitionConversion}
 * objects.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class WorkflowDefinitionConversionFactory {

  protected HashMap<Class< ? >, StepDefinitionConverter> stepConverters;
  protected List<WorkflowDefinitionConversionListener> workflowDefinitionConversionListeners;

  /**
   * @return a new, empty conversion to be used to store all converted
   *         artifacts.
   */
  public WorkflowDefinitionConversion createWorkflowDefinitionConversion() {
    return new WorkflowDefinitionConversion(this);
  }
  
  public WorkflowDefinitionConversion createWorkflowDefinitionConversion(WorkflowDefinition workflowDefinition) {
    return new WorkflowDefinitionConversion(this, workflowDefinition);
  }

  /**
   * @param stepConverters
   *          converter to register with this factory
   */
  public void setStepDefinitionConverters(List<StepDefinitionConverter> stepConverters) {
    this.stepConverters = new HashMap<Class< ? >, StepDefinitionConverter>();
    for (StepDefinitionConverter converter : stepConverters) {
      this.stepConverters.put(converter.getHandledClass(), converter);
    }
  }

  public List<WorkflowDefinitionConversionListener> getWorkflowDefinitionConversionListeners() {
    return workflowDefinitionConversionListeners;
  }

  public void setWorkflowDefinitionConversionListeners(List<WorkflowDefinitionConversionListener> workflowDefinitionConversionListeners) {
    this.workflowDefinitionConversionListeners = workflowDefinitionConversionListeners;
  }

  /**
   * @param definition
   *          step definition to get converter for.
   * @return Converter that can be used on the given definition.
   * @throws IllegalArgumentException
   *           when there is no converter known for the given definition.
   */
  public StepDefinitionConverter getStepConverterFor(StepDefinition definition) {
    final StepDefinitionConverter converter = stepConverters.get(definition.getClass());
    if (converter == null) {
      // TODO: i18n and error-handling
      throw new IllegalArgumentException("No converter found for step: " + definition.getClass());
    }
    return converter;
  }
}
