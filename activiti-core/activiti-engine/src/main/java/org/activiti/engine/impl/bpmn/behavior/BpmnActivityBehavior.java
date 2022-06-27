/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.bpmn.behavior;

import java.io.Serializable;
import java.util.List;

import org.activiti.engine.ActivitiEngineAgenda;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;

/**
 * Helper class for implementing BPMN 2.0 activities, offering convenience methods specific to BPMN 2.0.
 * <p>
 * This class can be used by inheritance or aggregation.
 */
public class BpmnActivityBehavior implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Performs the default outgoing BPMN 2.0 behavior, which is having parallel paths of executions for the outgoing sequence flow.
     * <p>
     * More precisely: every sequence flow that has a condition which evaluates to true (or which doesn't have a condition), is selected for continuation of the process instance. If multiple sequencer
     * flow are selected, multiple, parallel paths of executions are created.
     */
    public void performDefaultOutgoingBehavior(ExecutionEntity activityExecution) {
        performOutgoingBehavior(activityExecution,
                                true,
                                false);
    }

    /**
     * dispatch job canceled event for job associated with given execution entity
     * @param activityExecution
     */
    protected void dispatchJobCanceledEvents(ExecutionEntity activityExecution) {
        if (activityExecution != null) {
            List<JobEntity> jobs = activityExecution.getJobs();
            for (JobEntity job : jobs) {
                if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
                    Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED,
                                                                                                                                      job));
                }
            }

            List<TimerJobEntity> timerJobs = activityExecution.getTimerJobs();
            for (TimerJobEntity job : timerJobs) {
                if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
                    Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED,
                                                                                                                                      job));
                }
            }
        }
    }

    /**
     * Performs the default outgoing BPMN 2.0 behavior (@see {@link #performDefaultOutgoingBehavior(ExecutionEntity)}), but without checking the conditions on the outgoing sequence flow.
     * <p>
     * This means that every outgoing sequence flow is selected for continuing the process instance, regardless of having a condition or not. In case of multiple outgoing sequence flow, multiple
     * parallel paths of executions will be created.
     */
    public void performIgnoreConditionsOutgoingBehavior(ExecutionEntity activityExecution) {
        performOutgoingBehavior(activityExecution,
                                false,
                                false);
    }

    /**
     * Actual implementation of leaving an activity.
     * @param execution The current execution context
     * @param checkConditions Whether or not to check conditions before determining whether or not to take a transition.
     * @param throwExceptionIfExecutionStuck If true, an {@link ActivitiException} will be thrown in case no transition could be found to leave the activity.
     */
    protected void performOutgoingBehavior(ExecutionEntity execution,
                                           boolean checkConditions,
                                           boolean throwExceptionIfExecutionStuck) {
        getAgenda().planTakeOutgoingSequenceFlowsOperation(execution,
                                                                   true);
    }

    protected ActivitiEngineAgenda getAgenda() {
        return Context.getAgenda();
    }

}
