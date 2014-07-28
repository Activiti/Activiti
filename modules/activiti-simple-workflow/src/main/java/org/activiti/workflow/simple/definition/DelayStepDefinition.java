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
package org.activiti.workflow.simple.definition;

import java.util.HashMap;

import org.activiti.workflow.simple.exception.SimpleWorkflowException;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Step that delays the current process/execution for a certain amount of time. Can be either
 * a time-duration or a specific date.
 * 
 * @author Frederik Heremans
 *
 */
@JsonTypeName("delay-step")
public class DelayStepDefinition extends AbstractNamedStepDefinition {
	
  private static final long serialVersionUID = 1L;
  
  protected String timeDate;
  
  protected TimeDurationDefinition timeDuration;
  
  public void setTimeDate(String timeDate) {
	  this.timeDate = timeDate;
	  if(timeDate != null) {
	  	this.timeDuration = null;
	  }
  }
  
  public String getTimeDate() {
	  return timeDate;
  }
  
  public void setTimeDuration(TimeDurationDefinition timeDuration) {
	  this.timeDuration = timeDuration;
	  if(timeDuration != null) {
	  	this.timeDate = null;
	  }
  }
  
  public TimeDurationDefinition getTimeDuration() {
	  return timeDuration;
  }
  
	@Override
	public StepDefinition clone() {
		DelayStepDefinition definition = new DelayStepDefinition();
		definition.setValues(this);
		return definition;
	}
	

	@Override
	public void setValues(StepDefinition otherDefinition) {
		if(!(otherDefinition instanceof DelayStepDefinition)) {
			throw new SimpleWorkflowException("An instance of DelayStepDefinition is required to set values");
		}
		
		DelayStepDefinition delayDefinition = (DelayStepDefinition) otherDefinition;
		setId(delayDefinition.getId());
		setName(delayDefinition.getName());
		setDescription(delayDefinition.getDescription());
		setTimeDate(delayDefinition.getTimeDate());
		setTimeDuration(delayDefinition.getTimeDuration());
		if(delayDefinition.getParameters() != null) {
			setParameters(new HashMap<String, Object>(delayDefinition.getParameters()));
		}
	}

}
