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

import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.workflow.simple.converter.ConversionConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.DelayStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;

public class DelayStepDefinitionConverter extends BaseStepDefinitionConverter<DelayStepDefinition, IntermediateCatchEvent> {

  private static final long serialVersionUID = 1L;

	@Override
  public Class<? extends StepDefinition> getHandledClass() {
	  return DelayStepDefinition.class;
  }

	@Override
  protected IntermediateCatchEvent createProcessArtifact(DelayStepDefinition stepDefinition,
      WorkflowDefinitionConversion conversion) {

		IntermediateCatchEvent event = new IntermediateCatchEvent();
		event.setId(conversion.getUniqueNumberedId(ConversionConstants.INTERMEDIATE_EVENT_ID_PREVIX));
		event.setName(stepDefinition.getName());
		event.setDocumentation(stepDefinition.getDescription());
		
		TimerEventDefinition timer = new TimerEventDefinition();
		event.addEventDefinition(timer);
		if(stepDefinition.getTimeDate() != null) {
			timer.setTimeDate(stepDefinition.getTimeDate());
		} else if(stepDefinition.getTimeDuration() != null) {
			timer.setTimeDuration(stepDefinition.getTimeDuration().toISO8601DurationString());
		}
    addFlowElement(conversion, event, true);
    return event;
  }
}
