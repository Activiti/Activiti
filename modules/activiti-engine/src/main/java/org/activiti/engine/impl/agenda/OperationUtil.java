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
package org.activiti.engine.impl.agenda;

import java.util.Collection;
import java.util.List;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.JobEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityManager;

public class OperationUtil {

    public static void deleteDataRelatedToExecution(CommandContext commandContext, ExecutionEntity executionEntity) {

        // To start, deactivate the current incoming execution
        executionEntity.setEnded(true);
        executionEntity.setActive(false);

        // Get variables related to execution and delete them
        VariableInstanceEntityManager variableInstanceEntityManager = commandContext.getVariableInstanceEntityManager();
        Collection<VariableInstanceEntity> executionVariables = variableInstanceEntityManager.findVariableInstancesByExecutionId(executionEntity.getId());
        for (VariableInstanceEntity variableInstanceEntity : executionVariables) {
            variableInstanceEntityManager.delete(variableInstanceEntity);
            if (variableInstanceEntity.getByteArrayValueId() != null) {
                commandContext.getByteArrayEntityManager().deleteByteArrayById(variableInstanceEntity.getByteArrayValueId());
            }
        }

        // Delete current user tasks
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        Collection<TaskEntity> tasksForExecution = taskEntityManager.findTasksByExecutionId(executionEntity.getId());
        for (TaskEntity taskEntity : tasksForExecution) {
            taskEntityManager.delete(taskEntity);
        }

        // Delete jobs
        JobEntityManager jobEntityManager = commandContext.getJobEntityManager();
        Collection<JobEntity> jobsForExecution = jobEntityManager.findJobsByExecutionId(executionEntity.getId());
        for (JobEntity job : jobsForExecution) {
            jobEntityManager.delete(job);
        }
        
        // Delete event subscriptions 
        EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
        List<EventSubscriptionEntity> eventSubscriptions = executionEntity.getEventSubscriptions();
        for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
            eventSubscriptionEntityManager.deleteEventSubscription(eventSubscription); 
        }
    }
}
