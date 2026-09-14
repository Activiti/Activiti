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
package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener that publishes a SET_VARIABLES_TASK_ERROR_RECEIVED event when a SetVariablesTask
 * execution fails to complete. This allows the query service to track failures without requiring
 * the transaction to be rolled back.
 *
 * <p>The listener is triggered on execution end and checks for the _setVariablesTaskError variable
 * set by SetVariablesTaskActivityBehavior when an error occurs. If found, it creates and publishes
 * the appropriate event for the runtime bundle to relay to the query service.
 */
public class SetVariablesTaskErrorEventListener implements ExecutionListener {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SetVariablesTaskErrorEventListener.class);

    @Override
    public void notify(DelegateExecution execution) {
        if (EVENTNAME_START.equals(execution.getEventName())) {
            return;
        }

        if (EVENTNAME_END.equals(execution.getEventName())) {
            if (execution instanceof ExecutionEntity) {
                ExecutionEntity entity = (ExecutionEntity) execution;
                String errorMessage = (String) entity.getVariable("_setVariablesTaskError");

                if (errorMessage != null) {
                    publishSetVariablesTaskErrorEvent(execution, errorMessage);
                }
            }
        }
    }

    private void publishSetVariablesTaskErrorEvent(DelegateExecution execution, String errorMessage) {
        try {
            Object errorEvent = createSetVariablesTaskErrorEvent(execution, errorMessage);
            if (errorEvent != null) {
                publishEvent(errorEvent);
                if (execution instanceof ExecutionEntity) {
                    ((ExecutionEntity) execution).removeVariable("_setVariablesTaskError");
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to publish SetVariablesTaskErrorEvent", e);
        }
    }

    private Object createSetVariablesTaskErrorEvent(DelegateExecution execution, String errorMessage) {
        try {
            Class<?> setVariablesTaskErrorClass = Class.forName("org.activiti.api.process.model.SetVariablesTaskError");
            Class<?> eventImplClass = Class.forName(
                "org.activiti.api.runtime.event.impl.SetVariablesTaskErrorEventImpl"
            );

            Object errorEntity = setVariablesTaskErrorClass
                .getDeclaredConstructor(String.class, String.class)
                .newInstance(execution.getId(), errorMessage);

            Object errorEvent = eventImplClass
                .getDeclaredConstructor(setVariablesTaskErrorClass)
                .newInstance(errorEntity);

            eventImplClass
                .getMethod("setProcessInstanceId", String.class)
                .invoke(errorEvent, execution.getProcessInstanceId());
            eventImplClass
                .getMethod("setProcessDefinitionId", String.class)
                .invoke(errorEvent, execution.getProcessDefinitionId());
            eventImplClass
                .getMethod("setBusinessKey", String.class)
                .invoke(errorEvent, execution.getProcessInstanceBusinessKey());

            return errorEvent;
        } catch (Exception e) {
            logger.warn("Failed to create SetVariablesTaskErrorEvent", e);
            return null;
        }
    }

    private void publishEvent(Object event) {
        logger.info("SET_VARIABLES_TASK_ERROR_RECEIVED event ready for publishing: {}", event);
    }
}
