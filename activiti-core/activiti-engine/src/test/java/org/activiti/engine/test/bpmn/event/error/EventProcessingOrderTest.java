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
package org.activiti.engine.test.bpmn.event.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Tests for error event processing order (boundary events and event subprocesses).
 * Validates that error handlers with specific error codes are processed
 * before catch-all handlers (those with no error code).
 * This test class validates the fix in ErrorPropagation.findCatchingEventsForProcess()
 * which ensures specific error handlers take precedence over catch-all handlers
 * regardless of their order in the BPMN XML.
 */
public class EventProcessingOrderTest extends PluggableActivitiTestCase {

    /**
     * Tests that a catch-all boundary event executes when no specific error handler matches.
     * BPMN Structure:
     * - Subprocess throws error with code "ERROR_UNKNOWN"
     * - Boundary event with error code "ERROR_1" (specific) - won't match
     * - Boundary event with error code "ERROR_2" (specific) - won't match
     * - Boundary event with NO error code (catch-all) - should match
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
     * Tests that XML definition order does not affect error handler precedence.
     * BPMN Structure:
     * - Subprocess throws error with code "123"
     * - Boundary events defined in this XML order:
     *   1. Catch-all (no error code)
     *   2. ERROR_1 (specific)
     *   3. Error code "123" (specific) - SHOULD match despite being last
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
     * Tests that a catch-all event subprocess executes when no specific error handler matches.
     * BPMN Structure:
     * - Subprocess throws error with code "ERROR_UNKNOWN"
     * - Event subprocess with error code "ERROR_1" (specific) - won't match
     * - Event subprocess with error code "ERROR_2" (specific) - won't match
     * - Event subprocess with NO error code (catch-all) - should match
     * Expected: Catch-all event subprocess handler executes as fallback
     */
    @Deployment
    public void testEventSubprocessCatchAllExecutesWhenNoSpecificHandlerMatches() {
        String procId = runtimeService.startProcessInstanceByKey("eventSubprocessCatchAllFallback").getId();

        // The catch-all event subprocess handler should be executed since no specific handler matches
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo("Catch All Event Subprocess Task");
        assertThat(task.getTaskDefinitionKey()).isEqualTo("catchAllEventSubprocessTask");

        // Completing the task will end the process instance
        taskService.complete(task.getId());
        assertProcessEnded(procId);
    }

    /**
     * Tests that XML definition order does not affect event subprocess error handler precedence.
     * BPMN Structure:
     * - Subprocess throws error with code "123"
     * - Event subprocesses defined in this XML order:
     *   1. Catch-all (no error code) - defined FIRST
     *   2. ERROR_1 (specific)
     *   3. Error code "123" (specific) - SHOULD match despite being last
     * Expected: Event subprocess with error code "123" executes, not catch-all or ERROR_1
     */
    @Deployment
    public void testEventSubprocessXMLOrderDoesNotAffectPrecedence() {
        String procId = runtimeService.startProcessInstanceByKey("eventSubprocessXMLOrderIndependence").getId();

        // The specific event subprocess with code "123" should execute, despite catch-all being first in XML
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo("Event Subprocess Task for Error 123");
        assertThat(task.getTaskDefinitionKey()).isEqualTo("eventSubprocessTask123");

        // Completing the task will end the process instance
        taskService.complete(task.getId());
        assertProcessEnded(procId);
    }

    /**
     * Tests call activity error matching by error code when error IDs are different.
     * BPMN Structure:
     * - Child process throws error with id="ERR_CHILD" errorCode="BUSINESS_ERROR"
     * - Parent process has error definition id="ERR_PARENT" errorCode="BUSINESS_ERROR"
     * - Parent catches error with errorRef="ERR_PARENT"
     * Expected: Parent catches error by matching error code "BUSINESS_ERROR"
     */
    @Deployment
    public void testCallActivityErrorMatchByErrorCode() {
        String procId = runtimeService.startProcessInstanceByKey("parentProcess").getId();

        // Parent should catch error by matching error code "BUSINESS_ERROR"
        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo("Error Handled by Code");
        assertThat(task.getTaskDefinitionKey()).isEqualTo("errorHandledByCode");

        taskService.complete(task.getId());
        assertProcessEnded(procId);
    }
}
