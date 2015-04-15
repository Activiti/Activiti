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
package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.jobexecutor.TriggerTimerEventJobHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.util.TimerUtil;

public class IntermediateTimerCatchEventActivityBehavior extends AbstractBpmnActivityBehavior {
	
	protected TimerEventDefinition timerEventDefinition;
	
	public IntermediateTimerCatchEventActivityBehavior(TimerEventDefinition timerEventDefinition) {
		this.timerEventDefinition = timerEventDefinition;
	}

    public void execute(ActivityExecution execution) {
        TimerEntity timer = TimerUtil.createTimerEntityForTimerEventDefinition(timerEventDefinition, 
        		false, (ExecutionEntity) execution, TriggerTimerEventJobHandler.TYPE, execution.getCurrentActivityId());
        Context.getCommandContext().getJobEntityManager().schedule(timer);
    }

    @Override
    public void trigger(ActivityExecution execution, String triggerName, Object triggerData) {
        leave(execution);
    }
}
