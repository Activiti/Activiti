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
package org.activiti.engine.test.db;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import org.activiti.engine.impl.bpmn.behavior.MappingExecutionContext;
import org.activiti.engine.impl.bpmn.behavior.VariablesCalculator;
import org.activiti.engine.impl.bpmn.behavior.VariablesPropagator;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.assertj.core.groups.Tuple;

/**
 * Test demonstrating that concurrent variable propagation via {@link VariablesPropagator}
 * can produce duplicate-named variables in the database
 *
 * This simulates what happens when multiple concurrent connector results
 * (e.g. from parallel multi-instance service tasks) each propagate output variables
 * with the same name to the root process instance via separate REQUIRES_NEW transactions.
 */
public class DuplicateVariableInsertViaPropagatorTest extends PluggableActivitiTestCase {

    /**
     * Two threads concurrently propagate the same variable name via {@link VariablesPropagator}.
     * Under READ COMMITTED isolation, both threads see no existing variable and each inserts a new row,
     * resulting in duplicate-named variables in the database.
     */
    public void testBeAbleToDeleteProcessWithDuplicateVariableInsertViaVariablesPropagator() throws Exception {
        String processDefinitionId = deployOneTaskTestProcess();
        final ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId);

        final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        final String childExecutionId = task.getExecutionId();

        final CyclicBarrier startBarrier = new CyclicBarrier(2);
        final CyclicBarrier endBarrier = new CyclicBarrier(2);

        final List<Exception> exceptions = new ArrayList<Exception>();

        Thread firstThread = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        managementService.executeCommand(
                            new PropagateVariablesWithBarriersCommand(startBarrier, endBarrier, childExecutionId)
                        );
                    } catch (Exception e) {
                        exceptions.add(e);
                    }
                }
            }
        );

        Thread secondThread = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        managementService.executeCommand(
                            new PropagateVariablesWithBarriersCommand(startBarrier, endBarrier, childExecutionId)
                        );
                    } catch (Exception e) {
                        exceptions.add(e);
                    }
                }
            }
        );

        firstThread.start();
        secondThread.start();

        firstThread.join();
        secondThread.join();

        // Use getVariableInstancesByExecutionIds to load all variable instances (including duplicates)
        Set<String> executionIds = new HashSet<String>();
        executionIds.add(processInstance.getId());
        List<VariableInstance> variableInstances = runtimeService.getVariableInstancesByExecutionIds(executionIds);

        // Only one variable instance should exist (no duplicates)
        assertThat(variableInstances)
            .extracting(VariableInstance::getName, VariableInstance::getValue)
            .containsExactly(Tuple.tuple("var", "12345"), Tuple.tuple("var", "12345"));

        assertThat(exceptions).isEmpty();

        runtimeService.deleteProcessInstance(processInstance.getId(), "ShouldNotFail");
    }

    /**
     * Command that wraps {@link VariablesPropagator#propagate} with barrier synchronization
     * to guarantee concurrent execution of variable propagation from two threads.
     *
     * Uses a pass-through {@link VariablesCalculator} that returns variables as-is,
     * simulating the behavior of connector output variable propagation.
     */
    private class PropagateVariablesWithBarriersCommand implements Command<Void> {

        private final CyclicBarrier startBarrier;
        private final CyclicBarrier endBarrier;
        private final String executionId;

        public PropagateVariablesWithBarriersCommand(
            CyclicBarrier startBarrier,
            CyclicBarrier endBarrier,
            String executionId
        ) {
            this.startBarrier = startBarrier;
            this.endBarrier = endBarrier;
            this.executionId = executionId;
        }

        @Override
        public Void execute(CommandContext commandContext) {
            try {
                startBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }

            ExecutionEntity execution = commandContext.getExecutionEntityManager().findById(executionId);

            // Use a pass-through calculator that returns available variables as-is,
            // simulating what happens with connector output variables
            VariablesPropagator propagator = getVariablesPropagator();
            propagator.propagate(execution, singletonMap("var", "12345"));

            try {
                endBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        private VariablesPropagator getVariablesPropagator() {
            VariablesCalculator passThroughCalculator = new VariablesCalculator() {
                @Override
                public Map<String, Object> calculateOutPutVariables(
                    MappingExecutionContext mappingExecutionContext,
                    Map<String, Object> availableVariables
                ) {
                    return availableVariables;
                }

                @Override
                public Map<String, Object> calculateInputVariables(
                    org.activiti.engine.delegate.DelegateExecution execution
                ) {
                    return null;
                }
            };

            return new VariablesPropagator(passThroughCalculator);
        }
    }
}
