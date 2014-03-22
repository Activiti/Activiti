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

import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.StepDefinition;

/**
 * <p>
 * A class that is responsible for converting a single {@link StepDefinition} to
 * all required artifacts needed.
 * </p>
 * 
 * <p>
 * Please note that {@link StepDefinitionConverter} instances are reused and
 * should be state-less.
 * </p>
 * 
 * @author Frederik Heremans
 */
public interface StepDefinitionConverter<U extends StepDefinition, T> {

  /**
   * @return class that this converter is capable of handling.
   */
  Class< ? extends StepDefinition> getHandledClass();

  /**
   * Convert given {@link StepDefinition} to correct artifacts and adds them to
   * process, models and forms.
   * 
   * @param stepDefinition
   *          the {@link StepDefinition}
   * @param conversion
   *          The conversion which is calling this step converter.
   */
  T convertStepDefinition(StepDefinition stepDefinition, WorkflowDefinitionConversion conversion);
  
}
