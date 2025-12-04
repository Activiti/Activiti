/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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
package org.activiti.engine.test.bpmn.event.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Tests for boundary error event processing order.
 *
 * Validates that boundary events with specific error codes are processed
 * before catch-all boundary events (those with no error code).
 *
 * This test class validates the fix in ErrorPropagation.findCatchingEventsForProcess()
 * which ensures specific error handlers take precedence over catch-all handlers
 * regardless of their order in the BPMN XML.
 *
 * @see org.activiti.engine.impl.bpmn.helper.ErrorPropagation#findCatchingEventsForProcess
 */
public class BoundaryErrorEventProcessingOrderTest extends PluggableActivitiTestCase {

    /**
     * Tests that a boundary event with a specific error code takes precedence
     * over a catch-all boundary event, even when the catch-all is defined first in the XML.
     *
     * BPMN Structure:
     * - Subprocess throws error with code "123"
     * - Boundary event with NO error code (catch-all) - defined FIRST in XML
     * - Boundary event with error code "123" (specific) - defined SECOND in XML
     *
     * Expected: Specific error handler executes, NOT the catch-all
     */
    @Deployment
    public void testSpecificErrorHandlerTakesPrecedenceOverCatchAllRegardlessOfXMLOrder() {
        String procId = runtimeService.startProcessInstanceByKey("specificHandlerPrecedence").getId();

        // The specific error handler should be executed, not the catch-all
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo("Specific Error Handler Task");
        assertThat(task.getTaskDefinitionKey()).isEqualTo("specificErrorTask");

        // Completing the task will end the process instance
        taskService.complete(task.getId());
        assertProcessEnded(procId);
    }

    /**
     * Tests that a catch-all boundary event executes when no specific error handler matches.
     *
     * BPMN Structure:
     * - Subprocess throws error with code "ERROR_UNKNOWN"
     * - Boundary event with error code "ERROR_1" (specific) - won't match
     * - Boundary event with error code "ERROR_2" (specific) - won't match
     * - Boundary event with NO error code (catch-all) - should match
     *
     * Expected: Catch-all handler executes as fallback
     */
    @Deployment
    public void testCatchAllHandlerExecutesWhenNoSpecificHandlerMatches() {
        String procId = runtimeService.startProcessInstanceByKey("catchAllFallback").getId();

        // The catch-all handler should be executed since no specific handler matches
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo("Catch All Handler Task");
        assertThat(task.getTaskDefinitionKey()).isEqualTo("catchAllTask");

        // Completing the task will end the process instance
        taskService.complete(task.getId());
        assertProcessEnded(procId);
    }

    /**
     * Tests that when multiple specific error handlers exist, only the matching one executes.
     *
     * BPMN Structure:
     * - Subprocess throws error with code "ERROR_2"
     * - Boundary event with error code "ERROR_1" (specific) - won't match
     * - Boundary event with error code "ERROR_2" (specific) - SHOULD match
     * - Boundary event with error code "ERROR_3" (specific) - won't match
     * - Boundary event with NO error code (catch-all) - should not execute
     *
     * Expected: Only ERROR_2 handler executes
     */
    @Deployment
    public void testOnlyMatchingSpecificHandlerExecutes() {
        String procId = runtimeService.startProcessInstanceByKey("multipleSpecificHandlers").getId();

        // Only the ERROR_2 handler should be executed
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo("Error 2 Handler Task");
        assertThat(task.getTaskDefinitionKey()).isEqualTo("error2Task");

        // Verify only one task exists (not multiple handlers triggered)
        assertThat(taskService.createTaskQuery().count()).isEqualTo(1);

        // Completing the task will end the process instance
        taskService.complete(task.getId());
        assertProcessEnded(procId);
    }

    /**
     * Tests that XML definition order does not affect error handler precedence.
     *
     * BPMN Structure:
     * - Subprocess throws error with code "123"
     * - Boundary events defined in this XML order:
     *   1. Catch-all (no error code)
     *   2. ERROR_1 (specific)
     *   3. Error code "123" (specific) - SHOULD match despite being last
     *
     * Expected: Handler with error code "123" executes, not catch-all or ERROR_1
     */
    @Deployment
    public void testXMLOrderDoesNotAffectPrecedence() {
        String procId = runtimeService.startProcessInstanceByKey("xmlOrderIndependence").getId();

        // The specific handler with code "123" should execute, despite being last in XML
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo("Task for Error 123");
        assertThat(task.getTaskDefinitionKey()).isEqualTo("task123");

        // Completing the task will end the process instance
        taskService.complete(task.getId());
        assertProcessEnded(procId);
    }

    /**
     * Tests backward compatibility scenario where catch-all is defined before specific handlers.
     * This was the "broken" scenario that the fix addresses.
     *
     * BPMN Structure (mimics real-world scenario):
     * - Subprocess throws error with code "NOT_FOUND"
     * - Boundary events defined in this XML order:
     *   1. Catch-all (no error code) - defined FIRST
     *   2. NOT_FOUND (specific)
     *   3. TIMEOUT (specific)
     *
     * Expected: Specific NOT_FOUND handler executes, not catch-all
     * (Before fix, catch-all would have caught it due to XML order)
     */
    @Deployment
    public void testCatchAllDefinedBeforeSpecificInXML() {
        String procId = runtimeService.startProcessInstanceByKey("catchAllBeforeSpecific").getId();

        // The specific NOT_FOUND handler should execute, not the catch-all
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo("Not Found Handler Task");
        assertThat(task.getTaskDefinitionKey()).isEqualTo("notFoundTask");

        // Completing the task will end the process instance
        taskService.complete(task.getId());
        assertProcessEnded(procId);
    }
}
