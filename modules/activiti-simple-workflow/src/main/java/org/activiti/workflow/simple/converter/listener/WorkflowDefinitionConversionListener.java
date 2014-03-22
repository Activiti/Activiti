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
package org.activiti.workflow.simple.converter.listener;

import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversionFactory;
import org.activiti.workflow.simple.converter.step.StepDefinitionConverter;

/**
 * Allows to hook into the lifecycle of a {@link WorkflowDefinitionConversion}.
 * 
 * Instances of this class can be added to a {@link WorkflowDefinitionConversionFactory}
 * and the specific methods will be called during the conversion.
 * 
 * @see DefaultWorkflowDefinitionConversionListener
 * 
 * @author Joram Barrez
 */
public interface WorkflowDefinitionConversionListener {

  /**
   * Called when the {@link WorkflowDefinitionConversion} is initialized,
   * but nothing has been converted yet.
   */
  void beforeStepsConversion(WorkflowDefinitionConversion conversion);

  /**
   * Called when the {@link WorkflowDefinitionConversion} has called all
   * {@link StepDefinitionConverter} that were added to the {@link WorkflowDefinitionConversionFactory}.
   */
  void afterStepsConversion(WorkflowDefinitionConversion conversion);

}
