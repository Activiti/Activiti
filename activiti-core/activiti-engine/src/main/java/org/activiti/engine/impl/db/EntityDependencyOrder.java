/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.engine.impl.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.engine.impl.persistence.entity.AttachmentEntityImpl;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntityImpl;
import org.activiti.engine.impl.persistence.entity.CommentEntityImpl;
import org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntityImpl;
import org.activiti.engine.impl.persistence.entity.DeadLetterJobEntityImpl;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.activiti.engine.impl.persistence.entity.Entity;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntityImpl;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricDetailAssignmentEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricDetailEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricDetailTransitionInstanceEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricFormPropertyEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricScopeInstanceEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntityImpl;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntityImpl;
import org.activiti.engine.impl.persistence.entity.JobEntityImpl;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntityImpl;
import org.activiti.engine.impl.persistence.entity.ModelEntityImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntityImpl;
import org.activiti.engine.impl.persistence.entity.PropertyEntityImpl;
import org.activiti.engine.impl.persistence.entity.ResourceEntityImpl;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntityImpl;
import org.activiti.engine.impl.persistence.entity.SuspendedJobEntityImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.impl.persistence.entity.TimerJobEntityImpl;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityImpl;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntityImpl;

/**
 * Maintains a list of all the entities in order of dependency.
 */
public class EntityDependencyOrder {

    public static final List<Class<? extends Entity>> DELETE_ORDER;
    public static final List<Class<? extends Entity>> INSERT_ORDER;
    public static final Map<Class<? extends Entity>, Integer> UPDATE_ORDER;

    static {
        /*
         * In the comments below:
         *
         * 'FK to X' : X should be BELOW the entity
         *
         * 'FK from X': X should be ABOVE the entity
         *
         */
        List<Class<? extends Entity>> deleteOrder = new ArrayList<>();

        /* No FK */
        deleteOrder.add(PropertyEntityImpl.class);

        /* No FK */
        deleteOrder.add(AttachmentEntityImpl.class);

        /* No FK */
        deleteOrder.add(CommentEntityImpl.class);

        /* No FK */
        deleteOrder.add(EventLogEntryEntityImpl.class);

        /*
         * FK to Deployment
         * FK to ByteArray
         */
        deleteOrder.add(ModelEntityImpl.class);

        /*
         * FK to ByteArray
         */
        deleteOrder.add(JobEntityImpl.class);
        deleteOrder.add(TimerJobEntityImpl.class);
        deleteOrder.add(SuspendedJobEntityImpl.class);
        deleteOrder.add(DeadLetterJobEntityImpl.class);

        /*
         * FK to ByteArray
         * FK to Exeution
         */
        deleteOrder.add(VariableInstanceEntityImpl.class);

        /*
         * FK to ByteArray
         * FK to ProcessDefinition
         */
        deleteOrder.add(ProcessDefinitionInfoEntityImpl.class);

        /*
         * FK from ModelEntity
         * FK from JobEntity
         * FK from VariableInstanceEntity
         *
         * FK to DeploymentEntity
         */
        deleteOrder.add(ByteArrayEntityImpl.class);

        /*
         * FK from ModelEntity
         * FK from JobEntity
         * FK from VariableInstanceEntity
         *
         * FK to DeploymentEntity
         */
        deleteOrder.add(ResourceEntityImpl.class);

        /*
         * FK from ByteArray
         */
        deleteOrder.add(DeploymentEntityImpl.class);

        /*
         * FK to Execution
         */
        deleteOrder.add(EventSubscriptionEntityImpl.class);

        /*
         * FK to Execution
         */
        deleteOrder.add(CompensateEventSubscriptionEntityImpl.class);

        /*
         * FK to Execution
         */
        deleteOrder.add(MessageEventSubscriptionEntityImpl.class);

        /*
         * FK to Execution
         */
        deleteOrder.add(SignalEventSubscriptionEntityImpl.class);

        /*
         * FK to process definition
         * FK to Execution
         * FK to Task
         */
        deleteOrder.add(IdentityLinkEntityImpl.class);

        /*
         * FK from IdentityLink
         *
         * FK to Execution
         * FK to process definition
         */
        deleteOrder.add(TaskEntityImpl.class);

        /*
         * FK to Execution
         * FK to process definition
         */
        deleteOrder.add(IntegrationContextEntityImpl.class);

        /*
         * FK from VariableInstance
         * FK from EventSubscription
         * FK from IdentityLink
         * FK from Task
         *
         * FK to ProcessDefinition
         */
        deleteOrder.add(ExecutionEntityImpl.class);

        /*
         * FK from Task
         * FK from IdentityLink
         * FK from execution
         */
        deleteOrder.add(ProcessDefinitionEntityImpl.class);

        // History entities have no FK's

        deleteOrder.add(HistoricIdentityLinkEntityImpl.class);

        deleteOrder.add(HistoricActivityInstanceEntityImpl.class);
        deleteOrder.add(HistoricProcessInstanceEntityImpl.class);
        deleteOrder.add(HistoricTaskInstanceEntityImpl.class);
        deleteOrder.add(HistoricScopeInstanceEntityImpl.class);

        deleteOrder.add(HistoricVariableInstanceEntityImpl.class);

        deleteOrder.add(HistoricDetailAssignmentEntityImpl.class);
        deleteOrder.add(HistoricDetailTransitionInstanceEntityImpl.class);
        deleteOrder.add(HistoricDetailVariableInstanceUpdateEntityImpl.class);
        deleteOrder.add(HistoricFormPropertyEntityImpl.class);
        deleteOrder.add(HistoricDetailEntityImpl.class);

        DELETE_ORDER = List.copyOf(deleteOrder);
        INSERT_ORDER = List.copyOf(deleteOrder.reversed());

        Map<Class<? extends Entity>, Integer> updateOrder = new HashMap<>();
        for (int i = 0; i < INSERT_ORDER.size(); i++) {
            updateOrder.put(INSERT_ORDER.get(i), i);
        }
        UPDATE_ORDER = Map.copyOf(updateOrder);
    }
}
